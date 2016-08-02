package com.lucidchart.open.relate.macros

import com.lucidchart.open.relate._
import com.lucidchart.open.relate.interp._
import macrocompat.bundle
import scala.reflect.macros.blackbox.Context

@bundle
class ParseableImpl(val c: Context) {
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
        val paramTrees: Map[String, Tree] = params.map {
          case q"$optNameAst -> $optValueAst" =>
            val optName = optNameAst match {
              case Literal(Constant(optName: String)) => optName
              case name => c.abort(name.pos, "Keys must be literal strings")
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
            case "colMapping" => optValueAst match {
              case q"Map[..$tpts](..$params)" =>
                opts.copy(remapping = getRemapping(params))
            }
            case "snakeCase" => optValueAst match {
              case q"true" => opts.copy(snakeCase = true)
              case q"false" => opts.copy(snakeCase = false)
              case value => c.abort(value.pos, "snakeCase requires a literal true or false value")
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
          case q"$mods object $tname extends { ..$earlydefns } with ..$parents { $self => ..$body }" =>
            val typeName = tq"$tpname"

            q"""$mods object $tname extends { ..$earlydefns } with ..$parents { $self =>
              ..$body

              implicit val relateParseable: com.lucidchart.open.relate.Parseable[$typeName] = {
                ${newParseable(typeName, extractors, None)}
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
    val theApply = findApply(tpe)

    val params = theApply match {
      case Some(symbol) => symbol.paramLists.head
      case None => c.abort(c.enclosingPosition, "No apply function found")
    }

    val paramNames = params.map(_.name.toString).toSet
    opts.remapping.foreach { case (givenCol, tree) =>
      if (!paramNames.contains(givenCol)) {
        c.abort(tree.pos, s"$givenCol is not a member of $tpe")
      }
    }

    val input = generateCalls(params.map(CallData.fromSymbol(_, opts)))

    val comp = q"${tpe.typeSymbol.companion}"
    val typeName = tq"${weakTypeTag[A].tpe}"
    newParseable(typeName, input, Some(comp))
  }

  private def newParseable(tpe: Tree, extractors: List[Tree], comp: Option[Tree]): Tree = {
    val apply = comp.getOrElse(q"apply")

    q"""
      new com.lucidchart.open.relate.Parseable[$tpe] {
        def parse(row: com.lucidchart.open.relate.SqlRow): $tpe = {
          $apply(..$extractors)
        }
      }
    """
  }

  private def tupleValueString(tupleTree: Tree): String = {
    val remapAst = tupleTree match {
      case q"$aa($colLit).$arrow[..$tpts]($remapAst)" => remapAst
      case q"$col -> $remapAst" => remapAst
      case q"($col, $remapAst)" => remapAst
    }

    remapAst match {
      case Literal(Constant(remap: String)) => remap
      case value => c.abort(value.pos, "Remappings must be literal strings")
    }
  }

  case class CallData(name: Literal, tpt: Type, args: List[Type], isOption: Boolean)
  object CallData {
    def fromSymbol(sym: Symbol, opts: AnnotOpts): CallData = {
      val value = if (opts.snakeCase) {
        toSnakeCase(sym.name.toString)
      } else if (opts.remapping.contains(sym.name.toString)) {
        tupleValueString(opts.remapping(sym.name.toString))
      } else {
        sym.name.toString
      }

      val TypeRef(_, outerType, args) = sym.info
      val TypeRef(_, option, _) = typeOf[Option[Any]]

      CallData(Literal(Constant(value)), sym.info, args, outerType == option)
    }
  }

  private def generateCalls(callData: List[CallData]): List[Tree] = {
    callData.map { cd =>
      if (cd.isOption) {
        q"row.opt[${cd.args.head}](${cd.name})"
      } else {
        q"row[${cd.tpt}](${cd.name})"
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

  private def toSnakeCase(s: String): String = s.replaceAll(
    "([A-Z]+)([A-Z][a-z])", "$1_$2"
  ).replaceAll(
    "([a-z\\d])([A-Z])", "$1_$2"
  ).toLowerCase

  private def findApply(target: Type): Option[MethodSymbol] = {
    val companion: Type = target.companion

    val unapplyReturnTypes = getUnapplyReturnTypes(companion)
    val applies = getApplies(companion)
    findApplyUnapplyMatch(companion, applies, unapplyReturnTypes)
  }

  private def getReturnTypes(args: List[Type]): Option[List[Type]] = {
    args.head match {
      case t @ TypeRef(_, _, Nil) => Some(List(t))
      case t @ TypeRef(_, _, args) =>
        if (t <:< typeOf[Product]) Some(args)
        else Some(List(t))
      case _ => None
    }
  }

  private def getUnapplyReturnTypes(companion: Type): Option[List[Type]] = {
    val unapply = companion.decl(TermName("unapply"))
    val unapplySeq = companion.decl(TermName("unapplySeq"))
    val hasVarArgs = unapplySeq != NoSymbol

    val effectiveUnapply = Seq(unapply, unapplySeq).find(_ != NoSymbol) match {
      case None => c.abort(c.enclosingPosition, "No unapply or unapplySeq function found")
      case Some(s) => s.asMethod
    }

    effectiveUnapply.returnType match {
      case TypeRef(_, _, Nil) =>
        c.abort(c.enclosingPosition, s"Unapply of $companion has no parameters. Are you using an empty case class?")
        None

      case TypeRef(_, _, args) =>
        args.head match {
          case t @ TypeRef(_, _, Nil) => Some(List(t))
          case t @ TypeRef(_, _, args) =>
            import c.universe.definitions.TupleClass
            if (!TupleClass.seq.exists(tupleSym => t.baseType(tupleSym) ne NoType)) Some(List(t))
            else if (t <:< typeOf[Product]) Some(args)
            else None
          case _ => None
        }
      case _ => None
    }
  }

  private def getApplies(companion: Type): List[Symbol] = {
    companion.decl(TermName("apply")) match {
      case NoSymbol => c.abort(c.enclosingPosition, "No apply function found")
      case s => s.asTerm.alternatives
    }
  }

  private def findApplyUnapplyMatch(
    companion: Type,
    applies: List[Symbol],
    unapplyReturnTypes: Option[List[Type]]
  ): Option[MethodSymbol] = {
    val unapply = companion.decl(TermName("unapply"))
    val unapplySeq = companion.decl(TermName("unapplySeq"))
    val hasVarArgs = unapplySeq != NoSymbol

    applies.collectFirst {
      case (apply: MethodSymbol) if hasVarArgs && {
        val someApplyTypes = apply.paramLists.headOption.map(_.map(_.asTerm.typeSignature))
        val someInitApply = someApplyTypes.map(_.init)
        val someApplyLast = someApplyTypes.map(_.last)
        val someInitUnapply = unapplyReturnTypes.map(_.init)
        val someUnapplyLast = unapplyReturnTypes.map(_.last)
        val initsMatch = someInitApply == someInitUnapply
        val lastMatch = (for {
          lastApply <- someApplyLast
          lastUnapply <- someUnapplyLast
        } yield lastApply <:< lastUnapply).getOrElse(false)
        initsMatch && lastMatch
      } => apply
      case (apply: MethodSymbol) if apply.paramLists.headOption.map(_.map(_.asTerm.typeSignature)) == unapplyReturnTypes => apply
    }
  }

  private def expand(colLit: Tree, tree: Tree): (String, Tree) = {
    val col = colLit match {
      case Literal(Constant(col: String)) => col
      case _ => c.abort(colLit.pos, "Column names must be literal strings")
    }
    col -> tree
  }

  private def getRemapping(params: List[Tree]): Map[String, Tree] = {
    params.map {
      case tree @ q"$aa($colLit).$arrow[..$tpts]($remapLit)" => expand(colLit, tree)
      case tree @ q"$colLit -> $remapLit" => expand(colLit, tree)
      case tree @ q"($colLit, $remapLit)" => expand(colLit, tree)
      case tree => c.abort(tree.pos, "Remappings must be literal tuples")
    }.toMap
  }
}
