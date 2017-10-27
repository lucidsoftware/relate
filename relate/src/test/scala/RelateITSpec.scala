package com.lucidchart.relate

import java.sql.{Connection, DriverManager, SQLException}
import java.util.Properties
import org.specs2.mutable._
import org.specs2.specification.core.Fragments

trait Db {
  val url = "jdbc:h2:mem:test"
  val driver = "org.h2.Driver"
  val props = new Properties()
  props.put("MODE", "MySQL")

  // a little weird because Travis won't set empty env variable
  // protected[this] val (user, pass) = Option(System.getenv("MYSQL_USER")).fold(("dev", "dev")) {
  //   (_, Option(System.getenv("MYSQL_PASSWORD")).getOrElse(""))
  // }

  Class.forName(driver)

  def withConnection[A](f: Connection => A): A = {
    val connection = DriverManager.getConnection(url, props)
    f(connection)
  }

  def createDb(): Unit = {
    val noDbConn = DriverManager.getConnection(url, props)

    withConnection { implicit connection =>
      sql"""
        CREATE TABLE pokedex (
          id BIGINT NOT NULL AUTO_INCREMENT,
          name VARCHAR(50) NOT NULL,
          description VARCHAR(200) NOT NULL,
          PRIMARY KEY (id),
          KEY name_key (name)
        )
      """.execute()

      sql"""
        CREATE TABLE pokemon (
          id BIGINT NOT NULL AUTO_INCREMENT,
          pokedex_id BIGINT NOT NULL,
          level INT NOT NULL,
          trainer_id BIGINT,
          PRIMARY KEY(id),
          KEY trainer_key (trainer_id)
        )
      """.execute()

      sql"""
        CREATE TABLE professor_oaks_pokemon (
          id BIGINT NOT NULL AUTO_INCREMENT,
          name VARCHAR(50) NOT NULL,
          trainer_id BIGINT,
          PRIMARY KEY(id),
          KEY p_oaks_trainer_key (trainer_id)
        )
      """.execute()

      sql"""
        CREATE TABLE undefeated_trainers (
          id BIGINT NOT NULL AUTO_INCREMENT,
          name VARCHAR(50) NOT NULL,
          PRIMARY KEY(id)
        )
      """.execute()

      sql"""
        CREATE TABLE pagination (
          id BIGINT NOT NULL AUTO_INCREMENT,
          value INT NOT NULL,
          cond BOOL NOT NULL,
          other_key BIGINT NOT NULL,
          PRIMARY KEY(id),
          KEY other_key (other_key)
        )
      """.execute()

      sql"""
        INSERT INTO pokedex VALUES
          (1, 'Squirtle', 'a cute water turtle'),
          (2, 'Wartortle', 'more sassy evolved form of Squirtle'),
          (3, 'Blastoise', 'an awesome turtle with water cannons'),
          (4, 'Pikachu', 'an overrated electric mouse'),
          (5, 'Geodude', 'a rock with arms'),
          (6, 'Jigglypuff', 'whoever thought Jigglypuff would be a good idea was stupid'),
          (7, 'Magikarp', 'some say it is worthless')
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
          (1, 'Squirtle', NULL),
          (2, 'Bulbasaur', NULL),
          (3, 'Charmander', NULL)
      """.execute()

      sql"""
        INSERT INTO undefeated_trainers VALUES
          (1, 'Lass Haley'),
          (2, 'Youngster Jimmy'),
          (3, 'Gym Leader Brock')
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
    //sql"DROP DATABASE relate_it_tests".execute()
  }
}



class RelateITSpec extends Specification with Db {

  override def map(tests: =>Fragments) = step(createDb) ^ tests ^ step(deleteDb)

  def streamConnection = DriverManager.getConnection(url, props)
  def streamConnection2 = DriverManager.getConnection(url, props)

  case class PokedexEntry(
    id: Long,
    name: String,
    description: String
  )

  def pokedexParser(row: SqlRow) = {
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

  def pokemonParser(row: SqlRow) = {
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

  def starterParser(row: SqlRow) = {
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

      sql"""
        INSERT INTO pokedex (name, description) VALUES ($pokemonName, $pokemonDesc)
      """.execute()

      //now check if that record was correctly inserted
      val entries = sql"""
       SELECT id, name, description FROM pokedex WHERE name = $pokemonName
      """.asList(pokedexParser)

      (entries.size must_== 1) and (entries(0).name must_== pokemonName) and (entries(0).description must_== pokemonDesc)
    }

    "work for multi-insert" in withConnection { implicit connection =>
     val pokedexId = 8
     val records = List(
       (pokedexId, 16, Some(1L)),
       (pokedexId, 5, None),
       (pokedexId, 10, Some(2L))
     )

      sql"""
       INSERT INTO pokemon (pokedex_id, level, trainer_id) VALUES $records
     """.execute()

     //check if those records were inserted
      val pokemon = sql"""
       SELECT id, pokedex_id, level, trainer_id FROM pokemon WHERE pokedex_id = $pokedexId
     """.asList(pokemonParser)

     val levelAndTrainerIdFromDb = pokemon.map { pokemon =>
       (pokemon.level, pokemon.trainerId)
     }
     val levelAndTrainerIdFromTest = records.map { pokemon =>
       (pokemon._2, pokemon._3)
     }
     (pokemon.size must_== records.size) and (levelAndTrainerIdFromDb must_== levelAndTrainerIdFromTest)
    }

    "be able to retrieve an autogenerated key" in withConnection { implicit connection =>
      val id = sql"""
        INSERT INTO pokedex (name, description)
        VALUES ('Charmander', 'as adorable as a fire lizard can be')
      """.executeInsertLong
      true
    }

    // "be able to retrieve a list of autogenerated keys" in withConnection { implicit connection =>
    //   val newEntries = Array(
    //     PokedexEntry(-1, "Bulbasaur", "a weird leaf thing grows on its back"),
    //     PokedexEntry(-1, "Ivysaur", "hard to say if it's any different from Bulbasaur")
    //   )

    //   val ids = sql"""
    //     INSERT INTO pokedex (name, description) VALUES {tuples}
    //   """).expand { implicit query =>
    //     tupled("tuples", List("name", "description"), newEntries.size)
    //   }.onTuples("tuples", newEntries) { (entry, query) =>
    //     query.string("name", entry.name)
    //     query.string("description", entry.description)
    //   }.executeInsertLongs

    //   //not totally implemented yet
    //   ids.size must_== newEntries.size
    // }
  }

  "select" should {
    "work with asSingle (also tests retrieving null value)" in withConnection { implicit connection =>
      val pokemon = sql"""
        SELECT id, pokedex_id, level, trainer_id
        FROM pokemon
        WHERE id = ${7L}
      """.asSingle(pokemonParser)

      (pokemon.id must_== 7) and (pokemon.pokedexId must_== 4) and (pokemon.level must_== 3) and (pokemon.trainerId must_== None)
    }

    "work when asSingleOption should return a value" in withConnection { implicit connection =>
      val pokemon = sql"""
        SELECT id, pokedex_id, level, trainer_id
        FROM pokemon
        WHERE id = ${7L}
      """.asSingleOption(pokemonParser).get

      (pokemon.id must_== 7) and (pokemon.pokedexId must_== 4) and (pokemon.level must_== 3) and (pokemon.trainerId must_== None)
    }

    "work when asSingleOption should not return a value" in withConnection { implicit connection =>
      val pokemon = sql"""
        SELECT id, pokedex_id, level, trainer_id
        FROM pokemon
        WHERE id = -1
      """.asSingleOption(pokemonParser)

      pokemon must_== None
    }

    "work with asSet" in withConnection { implicit connection =>
      val squirtle = PokedexEntry(1, "Squirtle", "a cute water turtle")

      val pokemon = sql"""
        SELECT id, name, description
        FROM pokedex
        WHERE name = ${squirtle.name}
      """.asSet(pokedexParser)

      (pokemon.size must_== 1) and (pokemon must contain(squirtle))
    }

    "work with empty asSet" in withConnection { implicit connection =>
      val pokemon = sql"""
        SELECT id, name, description
        FROM pokedex
        WHERE id = -1
      """.asSet(pokedexParser)

      pokemon.size must_== 0
    }

    "work with asSeq" in withConnection { implicit connection =>
      val squirtle = PokedexEntry(1, "Squirtle", "a cute water turtle")
      val pikachu = PokedexEntry(4, "Pikachu", "an overrated electric mouse")

      val pokemon = sql"""
        SELECT id, name, description
        FROM pokedex
        WHERE name = ${squirtle.name} OR name = ${pikachu.name}
      """.asSeq(pokedexParser)

      (pokemon.size must_== 2) and (pokemon must contain(squirtle)) and (pokemon must contain(pikachu))
    }

    "work with empty asSeq" in withConnection { implicit connection =>
      val pokemon = sql"""
        SELECT id, name, description
        FROM pokedex
        WHERE id = -1
      """.asSeq(pokedexParser)

      (pokemon.size must_== 0)
    }

    "work with asIterable" in withConnection { implicit connection =>
      val names = Array("Wartortle", "Blastoise", "Geodude")

      val pokemon = sql"""
        SELECT id, name, description
        FROM pokedex
        WHERE name = ${names(0)} OR name = ${names(1)} OR name = ${names(2)}
      """.asIterable(pokedexParser)

      val iterableAsList = pokemon.map(_.name)
      (iterableAsList must contain(names(0))) and (iterableAsList must contain(names(1))) and (iterableAsList must contain(names(2)))
    }

    "work with empty asIterable" in withConnection { implicit connection =>
      val pokemon = sql"""
        SELECT id, name, description
        FROM pokedex
        WHERE id = -1
      """.asIterable(pokedexParser).toList

      pokemon.size must_== 0
    }

    "work for asList" in withConnection { implicit connection =>
      val jigglypuff = PokedexEntry(6, "Jigglypuff", "whoever thought Jigglypuff would be a good idea was stupid")
      val magikarp = PokedexEntry(7, "Magikarp", "some say it is worthless")
      val pokemon = sql"""
        SELECT id, name, description
        FROM pokedex
        WHERE name = ${jigglypuff.name} OR name = ${magikarp.name}
      """.asList(pokedexParser)

      (pokemon.size must_== 2) and (pokemon must contain(jigglypuff)) and (pokemon must contain(magikarp))
    }

    "work for empty asList" in withConnection { implicit connection =>
      val pokemon = sql"""
        SELECT id, name, description
        FROM pokedex
        WHERE id = -1
      """.asList(pokedexParser)

      pokemon.size must_== 0
    }

    "work for asMap" in withConnection { implicit connection =>
      val wartortle = PokedexEntry(2, "Wartortle", "more sassy evolved form of Squirtle")
      val blastoise = PokedexEntry(3, "Blastoise", "an awesome turtle with water cannons")

      val pokemon = sql"""
        SELECT id, name, description
        FROM pokedex
        WHERE name = ${wartortle.name} OR name = ${blastoise.name}
      """.asMap { row =>
        (row.string("name"), row.string("description"))
      }

      (pokemon(wartortle.name) must_== wartortle.description) and (pokemon(blastoise.name) must_== blastoise.description)
    }

    "work for empty asMap" in withConnection { implicit connection =>
      val pokemon = sql"""
        SELECT id, name, description
        FROM pokedex
        WHERE id = -1
      """.asMap { row =>
        (row.string("name"), row.string("description"))
      }

      pokemon.size must_== 0
    }

    "work for asIterator" in withConnection { implicit connection =>
      val wartortle = PokedexEntry(2, "Wartortle", "more sassy evolved form of Squirtle")
      val blastoise = PokedexEntry(3, "Blastoise", "an awesome turtle with water cannons")

      val pokemon = sql"""
        SELECT id, name, description
        FROM pokedex
        WHERE name = ${wartortle.name} OR name = ${blastoise.name}
      """.asIterator(pokedexParser)(streamConnection).toList

      (pokemon.size must_== 2) and (pokemon must contain(wartortle)) and (pokemon must contain(blastoise))
    }

    "work for empty asIterator" in withConnection { implicit connection =>
      val pokemon = sql"""
        SELECT id, name, description
        FROM pokedex
        WHERE id = -1
      """.asIterator(pokedexParser)(streamConnection2).toList

      pokemon.size must_== 0
    }

    case class Result(
      id: Long,
      value: Int
    )

    "work using expand for the IN clause" in withConnection { implicit connection =>
      val ids = Array(1L, 2L, 3L)

      val pokemonNames = sql"""
        SELECT id, name, description
        FROM pokedex
        WHERE id IN ($ids)
      """.asList(pokedexParser).map(_.name)

      (pokemonNames must contain("Squirtle")) and (pokemonNames must contain("Wartortle")) and (pokemonNames must contain("Blastoise"))
    }

    "fail to insert if given a select query" in withConnection { implicit connection =>
      def insertOnSelect = sql"""
        SELECT id, name, description
        FROM pokedex
      """.executeInsertLong

      insertOnSelect must throwA[SQLException]
    }
  }

  "update" should {
    "update matched rows and not update unmatched rows" in withConnection { implicit connection =>
      val correct = List(
        Starter(1L, "Squirtle", Some(1L)),
        Starter(2L, "Bulbasaur", None),
        Starter(3L, "Charmander", None)
      )

      sql"""
        UPDATE professor_oaks_pokemon
        SET trainer_id = ${1L}
        WHERE name = ${"Squirtle"}
      """.executeUpdate()

      val pokemon = sql"""
        SELECT id, name, trainer_id
        FROM professor_oaks_pokemon
      """.asList(starterParser)

      pokemon must_== correct
    }
  }

  "delete" should {
    "delete matched rows and not delete unmatched rows" in withConnection { implicit connection =>
      val correct = List("Lass Haley", "Youngster Jimmy")

      sql"""
        DELETE FROM undefeated_trainers
        WHERE name = ${"Gym Leader Brock"}
      """.execute()

      val trainers = sql"""
        SELECT name FROM undefeated_trainers
      """.asList(RowParser.string("name"))

      trainers must_== correct
    }
  }

}
