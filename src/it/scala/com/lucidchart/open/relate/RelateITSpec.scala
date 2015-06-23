package com.lucidchart.open.relate

import com.lucidchart.open.relate.interp._
import com.lucidchart.open.relate.Query._
import java.sql.{DriverManager, Connection, SQLException}
import org.specs2.mutable._
import org.specs2.specification.Fragments
import org.specs2.specification.Step

trait Db {
  val url = "jdbc:mysql://localhost/"
  val dbName = "relate_it_tests"
  val driver = "com.mysql.jdbc.Driver"
  val user = "dev"
  val pass = "dev"

  Class.forName(driver)

  def withConnection[A](f: Connection => A): A = {
    val connection = DriverManager.getConnection(url + dbName, user, pass)
    f(connection)
  }

  def createDb(): Unit = {
    val noDbConn = DriverManager.getConnection(url, user, pass)

    sql"DROP DATABASE IF EXISTS relate_it_tests".execute()(noDbConn)
    sql"CREATE DATABASE relate_it_tests".execute()(noDbConn)

    withConnection { implicit connection =>
      sql"""
        CREATE TABLE pokedex (
          id BIGINT NOT NULL AUTO_INCREMENT,
          name VARCHAR(50) NOT NULL,
          description VARCHAR(200) NOT NULL,
          PRIMARY KEY (id),
          KEY name_key (name)
        ) ENGINE=InnoDB CHARSET=utf8 COLLATE=utf8_general_ci
      """.execute()

      sql"""
        CREATE TABLE pokemon (
          id BIGINT NOT NULL AUTO_INCREMENT,
          pokedex_id BIGINT NOT NULL,
          level INT NOT NULL,
          trainer_id BIGINT,
          PRIMARY KEY(id),
          KEY trainer_key (trainer_id)
        ) ENGINE=InnoDB CHARSET=utf8 COLLATE=utf8_general_ci
      """.execute()

      sql"""
        CREATE TABLE professor_oaks_pokemon (
          id BIGINT NOT NULL AUTO_INCREMENT,
          name VARCHAR(50) NOT NULL,
          trainer_id BIGINT,
          PRIMARY KEY(id),
          KEY trainer_key (trainer_id)
        ) ENGINE=InnoDB CHARSET=utf8 COLLATE=utf8_general_ci
      """.execute()

      sql"""
        CREATE TABLE undefeated_trainers (
          id BIGINT NOT NULL AUTO_INCREMENT,
          name VARCHAR(50) NOT NULL,
          PRIMARY KEY(id)
        ) ENGINE=InnoDB CHARSET=utf8 COLLATE=utf8_general_ci
      """.execute()

      sql"""
        CREATE TABLE pagination (
          id BIGINT NOT NULL AUTO_INCREMENT,
          value INT NOT NULL,
          cond BOOL NOT NULL,
          other_key BIGINT NOT NULL,
          PRIMARY KEY(id),
          KEY other_key (other_key)
        ) ENGINE=InnoDB CHARSET=utf8 COLLATE=utf8_general_ci
      """.execute()

      sql"""
        INSERT INTO pokedex VALUES
          (1, "Squirtle", "a cute water turtle"),
          (2, "Wartortle", "Squirtle's more sassy evolved form"),
          (3, "Blastoise", "an awesome turtle with water cannons"),
          (4, "Pikachu", "an overrated electric mouse"),
          (5, "Geodude", "a rock with arms"),
          (6, "Jigglypuff", "whoever thought Jigglypuff would be a good idea was stupid"),
          (7, "Magikarp", "some say it's worthless")
      """.execute()

      sql"""
        INSERT INTO pokemon VALUES
          (1, 1, 4, NULL),
          (2, 1, 3, 1),
          (3, 5, 6, 2),
          (4, 6, 2, 1),
          (6, 3, 36, 1),
          (7, 4, 3, NULL)
      """.execute()

      sql"""
        INSERT INTO professor_oaks_pokemon VALUES
          (1, "Squirtle", NULL),
          (2, "Bulbasaur", NULL),
          (3, "Charmander", NULL)
      """.execute()

      sql"""
        INSERT INTO undefeated_trainers VALUES
          (1, "Lass Haley"),
          (2, "Youngster Jimmy"),
          (3, "Gym Leader Brock")
      """.execute()

      sql"""
        INSERT INTO pagination VALUES
          (1, 2, TRUE, 1),
          (2, 1, TRUE, 1),
          (3, 10, FALSE, 3),
          (4, 3, TRUE, 5),
          (5, 1, TRUE, 6),
          (6, 2, TRUE, 9),
          (7, 1, TRUE, 1),
          (8, 2, FALSE, 3),
          (9, 2, TRUE, 5),
          (10, 3, TRUE, 2)
      """.execute()
    }
  }

  def deleteDb(): Unit = withConnection { implicit connection =>
    sql"DROP DATABASE relate_it_tests".execute()
  }
}



class RelateITSpec extends Specification with Db {

  override def map(tests: =>Fragments) = Step(createDb) ^ tests ^ Step(deleteDb)

  def streamConnection = DriverManager.getConnection(url + dbName, user, pass)
  def streamConnection2 = DriverManager.getConnection(url + dbName, user, pass)

  case class PokedexEntry(
    id: Long,
    name: String,
    description: String
  )

  val pokedexParser = RowParser { row =>
    PokedexEntry(
      row.long("id"),
      row.string("name"),
      row.string("description")
    )
  }

  case class Pokemon(
    id: Long,
    pokedexId: Long,
    level: Int,
    trainerId: Option[Long]
  )

  val pokemonParser = RowParser { row =>
    Pokemon(
      row.long("id"),
      row.long("pokedex_id"),
      row.int("level"),
      row.longOption("trainer_id")
    )
  }

  case class Starter(
    id: Long,
    name: String,
    trainerId: Option[Long]
  )

  val starterParser = RowParser { row =>
    Starter(
      row.long("id"),
      row.string("name"),
      row.longOption("trainer_id")
    )
  }

  //check if statements all closed in all tests stmt.isClosed

  "insert" should {

    "work in the basic case (uses select to test)" in withConnection { implicit connection =>
      val pokemonName = "Snorlax"
      val pokemonDesc = "a sleepy Pokemon"

      SQL("""
        INSERT INTO pokedex (name, description) VALUES ({name}, {description})
      """).on { implicit query =>
        string("name", pokemonName)
        string("description", pokemonDesc)
      }.execute()

      //now check if that record was correctly inserted
      val entries = SQL("""
       SELECT id, name, description FROM pokedex WHERE name = {name}
      """).on { implicit query =>
       string("name", pokemonName)
      }.asList(pokedexParser)

      (entries.size must_== 1) and (entries(0).name must_== pokemonName) and (entries(0).description must_== pokemonDesc)
    }

    "work for multi-insert" in withConnection { implicit connection =>
     val pokedexId = 8
     val records = List(
       Pokemon(-1, pokedexId, 16, Some(1L)),
       Pokemon(-1, pokedexId, 5, None),
       Pokemon(-1, pokedexId, 10, Some(2L))
     )

     SQL("""
       INSERT INTO pokemon (pokedex_id, level, trainer_id) VALUES {tuples}
     """).expand { implicit query =>
       tupled("tuples", List("id", "level", "trainer"), records.size)
     }.onTuples("tuples", records) { (pokemon, query) =>
       query.long("id", pokemon.pokedexId)
       query.int("level", pokemon.level)
       query.longOption("trainer", pokemon.trainerId)
     }.execute()

     //check if those records were inserted
     val pokemon = SQL("""
       SELECT id, pokedex_id, level, trainer_id FROM pokemon WHERE pokedex_id = {pokedexId}
     """).on { implicit query =>
       long("pokedexId", pokedexId)
     }.asList(pokemonParser)

     val levelAndTrainerIdFromDb = pokemon.map { pokemon =>
       (pokemon.level, pokemon.trainerId)
     }
     val levelAndTrainerIdFromTest = records.map { pokemon =>
       (pokemon.level, pokemon.trainerId)
     }
     (pokemon.size must_== records.size) and (levelAndTrainerIdFromDb must_== levelAndTrainerIdFromTest)
    }

    "be able to retrieve an autogenerated key" in withConnection { implicit connection =>
      val id = SQL ("""
        INSERT INTO pokedex (name, description)
        VALUES ("Charmander", "as adorable as a fire lizard can be")
      """).executeInsertLong
      true
    }

    "be able to retrieve a list of autogenerated keys" in withConnection { implicit connection =>
      val newEntries = Array(
        PokedexEntry(-1, "Bulbasaur", "a weird leaf thing grows on its back"),
        PokedexEntry(-1, "Ivysaur", "hard to say if it's any different from Bulbasaur")
      )

      val ids = SQL("""
        INSERT INTO pokedex (name, description) VALUES {tuples}
      """).expand { implicit query =>
        tupled("tuples", List("name", "description"), newEntries.size)
      }.onTuples("tuples", newEntries) { (entry, query) =>
        query.string("name", entry.name)
        query.string("description", entry.description)
      }.executeInsertLongs

      //not totally implemented yet
      ids.size must_== newEntries.size
    }

    "ignore parameters not in the query" in withConnection { implicit connection =>
      val pokemonName = "Mewtwo"
      val pokemonDesc = "an angst filled Pokemon"

      SQL("""
        INSERT INTO pokedex (name, description) VALUES ({name}, {description})
      """).on { implicit query =>
        string("name", pokemonName)
        string("description", pokemonDesc)
        string("ignored", "should be ignored")
      }.execute()

      //now check if that record was correctly inserted
      val entries = SQL("""
       SELECT id, name, description FROM pokedex WHERE name = {name}
      """).on { implicit query =>
       string("name", pokemonName)
      }.asList(pokedexParser)

      (entries.size must_== 1) and (entries(0).name must_== pokemonName) and (entries(0).description must_== pokemonDesc)
    }

    "fail to work if a parameter is not provided" in withConnection { implicit connection =>
      def failure = SQL("""
        INSERT INTO pokedex VALUES (1, {name} "a description")
      """).executeInsertLong

      failure must throwA[SQLException]
    }

    "fail to work if a wrong list is provided" in withConnection { implicit connection =>
      def failure = SQL("""
        INSERT INTO pokedex VALUES {tuples}
      """).expand { implicit query =>
        tupled("tuples", List("id", "name", "description"), 2)
      }.onTuples("wrong", List((1, "name", "description"), (2, "name", "description"))) { (tuple, query) =>
        query.long("id", tuple._1)
        query.string("name", tuple._2)
        query.string("description", tuple._3)
      }.executeInsertLongs

      failure must throwA[java.util.NoSuchElementException]
    }
  }

  "select" should {
    "work with asSingle (also tests retrieving null value)" in withConnection { implicit connection =>
      val pokemon = SQL("""
        SELECT id, pokedex_id, level, trainer_id
        FROM pokemon
        WHERE id = {id}
      """).on { implicit query =>
        long("id", 7L)
      }.asSingle(pokemonParser)

      (pokemon.id must_== 7) and (pokemon.pokedexId must_== 4) and (pokemon.level must_== 3) and (pokemon.trainerId must_== None)
    }

    "work when asSingleOption should return a value" in withConnection { implicit connection =>
      val pokemon = SQL("""
        SELECT id, pokedex_id, level, trainer_id
        FROM pokemon
        WHERE id = {id}
      """).on { implicit query =>
        long("id", 7L)
      }.asSingleOption(pokemonParser).get

      (pokemon.id must_== 7) and (pokemon.pokedexId must_== 4) and (pokemon.level must_== 3) and (pokemon.trainerId must_== None)
    }

    "work when asSingleOption should not return a value" in withConnection { implicit connection =>
      val pokemon = SQL("""
        SELECT id, pokedex_id, level, trainer_id
        FROM pokemon
        WHERE id = -1
      """).asSingleOption(pokemonParser)

      pokemon must_== None
    }

    "work with asSet" in withConnection { implicit connection =>
      val squirtle = PokedexEntry(1, "Squirtle", "a cute water turtle")

      val pokemon = SQL("""
        SELECT id, name, description
        FROM pokedex
        WHERE name = {name}
      """).on { implicit query =>
        string("name", squirtle.name)
      }.asSet(pokedexParser)

      (pokemon.size must_== 1) and (pokemon must contain(squirtle))
    }

    "work with empty asSet" in withConnection { implicit connection =>
      val pokemon = SQL("""
        SELECT id, name, description
        FROM pokedex
        WHERE id = -1
      """).asSet(pokedexParser)

      pokemon.size must_== 0
    }

    "work with asSeq" in withConnection { implicit connection =>
      val squirtle = PokedexEntry(1, "Squirtle", "a cute water turtle")
      val pikachu = PokedexEntry(4, "Pikachu", "an overrated electric mouse")

      val pokemon = SQL("""
        SELECT id, name, description
        FROM pokedex
        WHERE name = {name1} OR name = {name2}
      """).on { implicit query =>
        string("name1", squirtle.name)
        string("name2", pikachu.name)
      }.asSeq(pokedexParser)

      (pokemon.size must_== 2) and (pokemon must contain(squirtle)) and (pokemon must contain(pikachu))
    }

    "work with empty asSeq" in withConnection { implicit connection =>
      val pokemon = SQL("""
        SELECT id, name, description
        FROM pokedex
        WHERE id = -1
      """).asSeq(pokedexParser)

      (pokemon.size must_== 0)
    }

    "work with asIterable" in withConnection { implicit connection =>
      val names = Array("Wartortle", "Blastoise", "Geodude")

      val pokemon = SQL("""
        SELECT id, name, description
        FROM pokedex
        WHERE name = {name1} OR name = {name2} OR name = {name3}
      """).on { implicit query =>
        string("name1", names(0))
        string("name2", names(1))
        string("name3", names(2))
      }.asIterable(pokedexParser)

      val iterableAsList = pokemon.map(_.name)
      (iterableAsList must contain(names(0))) and (iterableAsList must contain(names(1))) and (iterableAsList must contain(names(2)))
    }

    "work with empty asIterable" in withConnection { implicit connection =>
      val pokemon = SQL("""
        SELECT id, name, description
        FROM pokedex
        WHERE id = -1
      """).asIterable(pokedexParser).toList

      pokemon.size must_== 0
    }

    "work for asList" in withConnection { implicit connection =>
      val jigglypuff = PokedexEntry(6, "Jigglypuff", "whoever thought Jigglypuff would be a good idea was stupid")
      val magikarp = PokedexEntry(7, "Magikarp", "some say it's worthless")
      val pokemon = SQL("""
        SELECT id, name, description
        FROM pokedex
        WHERE name = {name1} OR name = {name2}
      """).on { implicit query =>
        string("name1", jigglypuff.name)
        string("name2", magikarp.name)
      }.asList(pokedexParser)

      (pokemon.size must_== 2) and (pokemon must contain(jigglypuff)) and (pokemon must contain(magikarp))
    }

    "work for empty asList" in withConnection { implicit connection =>
      val pokemon = SQL("""
        SELECT id, name, description
        FROM pokedex
        WHERE id = -1
      """).asList(pokedexParser)

      pokemon.size must_== 0
    }

    "work for asMap" in withConnection { implicit connection =>
      val wartortle = PokedexEntry(2, "Wartortle", "Squirtle's more sassy evolved form")
      val blastoise = PokedexEntry(3, "Blastoise", "an awesome turtle with water cannons")

      val pokemon = SQL("""
        SELECT id, name, description
        FROM pokedex
        WHERE name = {name1} OR name = {name2}
      """).on { implicit query =>
        string("name1", wartortle.name)
        string("name2", blastoise.name)
      }.asMap(RowParser { row =>
        (row.string("name"), row.string("description"))
      })

      (pokemon(wartortle.name) must_== wartortle.description) and (pokemon(blastoise.name) must_== blastoise.description)
    }

    "work for empty asMap" in withConnection { implicit connection =>
      val pokemon = SQL("""
        SELECT id, name, description
        FROM pokedex
        WHERE id = -1
      """).asMap(RowParser { row =>
        (row.string("name"), row.string("description"))
      })

      pokemon.size must_== 0
    }

    "work for asIterator" in withConnection { implicit connection =>
      val wartortle = PokedexEntry(2, "Wartortle", "Squirtle's more sassy evolved form")
      val blastoise = PokedexEntry(3, "Blastoise", "an awesome turtle with water cannons")

      val pokemon = SQL("""
        SELECT id, name, description
        FROM pokedex
        WHERE name = {name1} OR name = {name2}
      """).on { implicit query =>
        string("name1", wartortle.name)
        string("name2", blastoise.name)
      }.asIterator(pokedexParser)(streamConnection).toList

      (pokemon.size must_== 2) and (pokemon must contain(wartortle)) and (pokemon must contain(blastoise))
    }

    "work for empty asIterator" in withConnection { implicit connection =>
      val pokemon = SQL("""
        SELECT id, name, description
        FROM pokedex
        WHERE id = -1
      """).asIterator(pokedexParser)(streamConnection2).toList

      pokemon.size must_== 0
    }

    "work for the limit offset implementation of paginated" in withConnection { implicit connection =>
      val ids = List(1L, 2L, 3L)
      val correctPokemonNames = List("Squirtle", "Wartortle", "Blastoise")

      val pokemonNames = PaginatedQuery(pokedexParser, 2, 0) {
        SQL("""
          SELECT id, name, description
          FROM pokedex
          WHERE id IN ({ids})
          ORDER BY id
        """).expand { implicit query =>
          commaSeparated("ids", ids.size)
        }.on { implicit query =>
          longs("ids", ids)
        }
      }.foldLeft(List[String]()) { case (result, current) =>
        current.name :: result
      }.reverse

      pokemonNames must_== correctPokemonNames
    }

    "work for the limit offset implementation of paginated when empty" in withConnection { implicit connection =>
      val pokemonNames = PaginatedQuery(pokedexParser, 2, 0) {
        SQL("""
          SELECT id, name, description
          FROM pokedex
          WHERE id = -1
        """)
      }.foldLeft(List[String]()) { case (result, current) =>
        current.name :: result
      }

      pokemonNames.size must_== 0
    }

    case class Result(
      id: Long,
      value: Int
    )

    "work for PaginatedQuery" in withConnection { implicit connection =>
      val parser = RowParser { row =>
        Result(row.long("id"), row.int("value"))
      }

      val sums = PaginatedQuery(parser) { lastRecordOption =>
        SQL("""
          SELECT id, value
          FROM pagination
          WHERE id > {id} AND cond={cond}
          ORDER BY id ASC
          LIMIT 5
        """).on {implicit query =>
          long("id", lastRecordOption.map(_.id).getOrElse(0))
          bool("cond", true)
        }
      }.foldLeft(0) { case (result, current) =>
        result + current.value
      }

      sums must_== 15
    }

    "work using expand for the IN clause" in withConnection { implicit connection =>
      val ids = Array(1L, 2L, 3L)

      val pokemonNames = SQL("""
        SELECT id, name, description
        FROM pokedex
        WHERE id IN ({ids})
      """).expand { implicit query =>
        commaSeparated("ids", ids.size)
      }.on { implicit query =>
        longs("ids", ids)
      }.asList(pokedexParser).map(_.name)

      (pokemonNames must contain("Squirtle")) and (pokemonNames must contain("Wartortle")) and (pokemonNames must contain("Blastoise"))
    }

    "fail to insert if given a select query" in withConnection { implicit connection =>
      def insertOnSelect = SQL("""
        SELECT id, name, description
        FROM pokedex
      """).executeInsertLong

      insertOnSelect must throwA[SQLException]
    }

    "fail if given a list with the wrong name" in withConnection { implicit connection =>
      def failure = SQL("""
        SELECT id, name, description
        FROM pokedex
        WHERE id in ({ids})
      """).expand { implicit query =>
        commaSeparated("ids", 3)
      }.on { implicit query =>
        longs("wrong", List(2L, 3L, 4L))
      }.asList(pokedexParser)

      failure must throwA[SQLException]
    }
  }

  "update" should {
    "update matched rows and not update unmatched rows" in withConnection { implicit connection =>
      val correct = List(
        Starter(1L, "Squirtle", Some(1L)),
        Starter(2L, "Bulbasaur", None),
        Starter(3L, "Charmander", None)
      )

      SQL("""
        UPDATE professor_oaks_pokemon
        SET trainer_id = {id}
        WHERE name = {name}
      """).on { implicit query =>
        long("id", 1L)
        string("name", "Squirtle")
      }.executeUpdate()

      val pokemon = SQL("""
        SELECT id, name, trainer_id
        FROM professor_oaks_pokemon
      """).asList(starterParser)

      pokemon must_== correct
    }
  }

  "delete" should {
    "delete matched rows and not delete unmatched rows" in withConnection { implicit connection =>
      val correct = List("Lass Haley", "Youngster Jimmy")

      SQL("""
        DELETE FROM undefeated_trainers
        WHERE name = {name}
      """).on { implicit query =>
        string("name", "Gym Leader Brock")
      }.execute()

      val trainers = SQL("""
        SELECT name FROM undefeated_trainers
      """).asList(RowParser.string("name"))

      trainers must_== correct
    }
  }

}
