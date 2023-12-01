package com.lucidchart.relate.macros

import scala.reflect.macros.blackbox.Context

class RowParserImpl(val c: Context) {
  import c.universe._

  def generateSnakeImpl[A: c.WeakTypeTag]: Tree = generate[A](AnnotOpts(true, Map.empty))
  def generateImpl[A: c.WeakTypeTag]: Tree = generate[A](AnnotOpts(false, Map.empty))
  def generateMappingImpl[A: c.WeakTypeTag](
    colMapping: c.Expr[Map[String, String]]
  ): Tree = {
    val q"$s(..$params)" = colMapping.tree

    generate[A](AnnotOpts(false, getRemapping(params)))
  }

  case class AnnotOpts(snakeCase: Boolean, remapping: Map[String, Tree])

  def annotation(annottees: c.Expr[Any]*): Tree = {
    val validOptions = Set(
      "colMapping",
      "snakeCase"
    )

    val opts: AnnotOpts = c.prefix.tree match {
      case q"new Record(..$params)" =>
        val paramTrees: Map[String, Tree] = params.map { case q"$optNameAst -> $optValueAst" =>
          val optName = optNameAst match {
            case Literal(Constant(optName: String)) => optName
            case name                               => c.abort(name.pos, "Keys must be literal strings")
          }

          if (!validOptions.contains(optName)) {
            c.abort(optNameAst.pos, s"$optName is an invalid option. Valid options: ${validOptions.mkString(", ")}")
          }

          optName -> optValueAst
        }.toMap

        if (paramTrees.contains("colMapping") && paramTrees.contains("snakeCase")) {
          c.abort(c.enclosingPosition, "Only one of snakeCase or colMapping can be supplied")
        }

        paramTrees.foldLeft(AnnotOpts(false, Map.empty)) { case (opts, (optName, optValueAst)) =>
          optName match {
            case "colMapping" =>
              optValueAst match {
                case q"Map[..$tpts](..$params)" =>
                  opts.copy(remapping = getRemapping(params))
              }
            case "snakeCase" =>
              optValueAst match {
                case q"true"  => opts.copy(snakeCase = true)
                case q"false" => opts.copy(snakeCase = false)
                case value    => c.abort(value.pos, "snakeCase requires a literal true or false value")
              }
          }
        }
      case q"new Record()" => AnnotOpts(false, Map.empty)
    }

    val inputs = annottees.map(_.tree).toList

    val result: List[Tree] = inputs match {
      case target @ q"case class $tpname[..$tparams] $ctorMods(...$paramss) extends { ..$earlydefns } with ..$parents { $self => ..$stats }" :: tail =>
        val params = paramss.head

        val paramNames = params.map(_.name.toString).toSet
        opts.remapping.foreach { case (givenCol, tree) =>
          if (!paramNames.contains(givenCol)) {
            c.abort(tree.pos, s"$givenCol is not a member of $tpname")
          }
        }

        val extractors = generateExtractors(params, opts)

        val existingCompanion = if (tail.isEmpty) {
          q"object ${tpname.toTermName} {  }"
        } else {
          tail.head
        }

        val companion: Tree = existingCompanion match {
          case q"$mods object $tname extends { ..$earlydefns } with ..$parents { $self: $stype => ..$body }" =>
            val typeName = tq"$tpname"

            q"""$mods object $tname extends { ..$earlydefns } with ..$parents { $self: $stype =>
              ..$body

              implicit val relateRowParser: com.lucidchart.relate.RowParser[$typeName] = {
                ${newRowParser(typeName, extractors, q"$tname")}
              }
            }"""
        }

        List(target.head, companion)
      case _ =>
        c.abort(c.enclosingPosition, "@Record must be used on a case class")
    }

    Block(result, Literal(Constant(())))
  }

  private def generate[A: c.WeakTypeTag](opts: AnnotOpts): Tree = {
    val tpe = weakTypeTag[A].tpe
    val fields = findCaseClassFields(tpe)

    val paramNames = fields.map(_._1.toString).toSet
    opts.remapping.foreach { case (givenCol, tree) =>
      if (!paramNames.contains(givenCol)) {
        c.abort(tree.pos, s"$givenCol is not a member of $tpe")
      }
    }

    val input = generateCalls(fields, opts)

    val comp = q"${tpe.typeSymbol.companion}"
    val typeName = tq"${weakTypeTag[A].tpe}"
    newRowParser(typeName, input, comp)
  }

  private def newRowParser(tpe: Tree, extractors: List[Tree], comp: Tree): Tree = {
    q"""
      new com.lucidchart.relate.RowParser[$tpe] {
        def parse(row: com.lucidchart.relate.SqlRow): $tpe = {
          $comp(..$extractors)
        }
      }
    """
  }

  private def tupleValueString(tupleTree: Tree): String = {
    val remapAst = tupleTree match {
      case q"$aa($colLit).$arrow[..$tpts]($remapAst)" => remapAst
      case q"$col -> $remapAst"                       => remapAst
      case q"($col, $remapAst)"                       => remapAst
    }

    remapAst match {
      case Literal(Constant(remap: String)) => remap
      case value                            => c.abort(value.pos, "Remappings must be literal strings")
    }
  }

  private def generateCalls(fields: List[(TermName, Type)], opts: AnnotOpts): List[Tree] = {
    fields.map { case (name, ty) =>
      val value = if (opts.snakeCase) {
        toSnakeCase(name.toString)
      } else if (opts.remapping.contains(name.toString)) {
        tupleValueString(opts.remapping(name.toString))
      } else {
        name.toString
      }

      val nameLiteral = Literal(Constant(value))

      val TypeRef(_, outerType, args) = ty
      val TypeRef(_, option, _) = typeOf[Option[Any]]

      if (outerType == option) {
        q"row.opt[${args.head}](${nameLiteral})"
      } else {
        q"row[${ty}](${nameLiteral})"
      }
    }
  }

  private def generateExtractors(params: List[ValDef], opts: AnnotOpts): List[Tree] = {
    params.map { param =>
      val p = {
        val value = if (opts.snakeCase) {
          toSnakeCase(param.name.toString)
        } else if (opts.remapping.contains(param.name.toString)) {
          tupleValueString(opts.remapping(param.name.toString))
        } else {
          param.name.toString
        }
        Literal(Constant(value))
      }

      param.tpt match {
        case AppliedTypeTree(outer, ps) if outer.toString == "Option" =>
          q"row.opt[${ps.head}]($p)"
        case tpt =>
          q"row[$tpt]($p)"
      }
    }
  }

  private def toSnakeCase(s: String): String = s
    .replaceAll(
      "([A-Z]+)([A-Z][a-z])",
      "$1_$2"
    )
    .replaceAll(
      "([a-z\\d])([A-Z])",
      "$1_$2"
    )
    .toLowerCase

  private def findCaseClassFields(ty: Type): List[(TermName, Type)] = {
    ty.members.sorted.collect {
      case m: MethodSymbol if m.isCaseAccessor => (m.name, m.returnType)
    }.toList
  }

  private def expand(colLit: Tree, tree: Tree): (String, Tree) = {
    val col = colLit match {
      case Literal(Constant(col: String)) => col
      case _                              => c.abort(colLit.pos, "Column names must be literal strings")
    }
    col -> tree
  }

  private def getRemapping(params: List[Tree]): Map[String, Tree] = {
    params.map {
      case tree @ q"$aa($colLit).$arrow[..$tpts]($remapLit)" => expand(colLit, tree)
      case tree @ q"$colLit -> $remapLit"                    => expand(colLit, tree)
      case tree @ q"($colLit, $remapLit)"                    => expand(colLit, tree)
      case tree                                              => c.abort(tree.pos, "Remappings must be literal tuples")
    }.toMap
  }
}
