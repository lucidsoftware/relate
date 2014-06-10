Relate
======

Relate is a lightweight database access layer for Scala that simplifies database interaction while leaving complete control over the actual SQL query. It was developed at Lucid Software in response to a need for increasingly performant database access software and seeks to abstract away the PreparedStatement's idiosyncrasies while maintaining its speed.

### Advantages – Why Use Relate?

* Works with all JDBC connection types
* Works with all database engines
* Performance
* 

### Constraints – What Relate Doesn't Do

* Create connections to databases
* ORM abstraction
 
### Basics – Writing Queries and Inserting Parameters

The core action in using Relate is writing SQL queries that contain named parameters and then calling functions to replace those parameters with their values. Here's a simple example to start with:

```
import com.lucidchart.open.relate._
import com.lucidchart.open.relate.Query._

SQL(“””
  UPDATE pokemon
  SET move2={move}
  WHERE id={id} AND name={name}
“””).on { implicit query =>
  int(“id”, 25)
  string(“name”, “Pikachu”)
  string(“move”, “Thundershock”)
}.executeUpdate()(connection)

```

This code snippet is faintly similar to using PreparedStatement. One must first write a SQL query, which is done in Relate by passing the query to the SQL function as a string. Placeholders in the query are specified by placing a parameter name between curly braces.

Next, the on method is used to insert values into query parameters. Parameter insertion is performed in an anonymous function passed to on. Within the function, methods with names corresponding to parameter type are called with parameter name and value as arguments. For convenience, if the query object is declared as an implicit parameter and there is an import for com.lucidchart.open.relate.Query._ in scope, insertion methods can be called directly without calling them on the query object.

Finally, executing the query requires a java.sql.Connection parameter in a second parameter list. If the Connection is in the implicit scope, it does not need to be provided to the execute method. All queries defined in PreparedStatement (execute, executeInsert, executeQuery, executeUpdate) are also defined in Relate.

WARNING: Because Relate uses curly braces as parameter delimiters, and to avoid additional overhead in query parsing, query strings containing curly braces will not work. However, curly braces can be used in values inserted in the on method. For example,

```
SQL(“””
  UPDATE pack
  SET items= “[{\”name\” : \”Pokeball\”, \”quantity\” : 2}]”
“””)
```

will result in errors, but 

```
SQL(“””
  UPDATE pack
  SET items={value}
“””).on { implicit query =>
  string(“value”, “[{\”name\” : \”Pokeball\”, \”quantity\” : 2}]”)
}
```

would be just fine.

### Parsers – Retrieving Data

##### Defining a Parser

Parsers are created by passing an anonymous function that takes a row object and returns some value to the RowParser's apply method. Within that function, values can be extracted from the row object. Here's an example:

```
import com.lucidchart.open.relate.RowParser

case class Pokemon(
  name: String,
  level: Short,
  trainerId: Option[Long]
)

val pokemonParser = RowParser { row =>
  Pokemon(
    row.string(“name”),
    row.short(“level”),
    row.longOption(“trainer_id”)
  )
}
```

In this example, the created parser takes the value from the “name” column of the row as a string, the value of “level” as a short, and the value from the “trainer_id” column as a long option to instantiate a Pokemon object. The row object has numerous methods to extract data from the row with the desired data type.

Some of the methods that can be called on the row object are prepended with the word “strict.” These methods are faster than their non strict counterparts, but do no type checking, and do not handle null values in the database.

The function passed to RowParser can return any value, so it does necessarily need to be an instance of a case class. A Tuple, Seq, etc. would work equally well.

##### Using a Parser

Applying a parser to a query only requires specifying the desired collection type to return. The following is an example using the parser created in the previous section:

```
SQL(“””
  SELECT *
  FROM professor_oaks_pokemon
  WHERE pokemon_type={type}
“””).on { implicit query =>
  string(“type”, “starter”)
}.executeQuery()(connection).asList(pokemonParser)
```

This example would return a List of Pokemon. The parser can also be passed to the asSingle, asSingleOption, asSet, asSeq, asIterable, and asList methods to produce the respective collections.

Parsers that return a Tuple of size 2 can also be passed to the asMap method to get a Map. Here's an example of its use (using the case class from the previous example):

```
val nameToPokemonParser  = RowParser { row =>
  val pokemon = Pokemon(
    row.string(“name”),
    row.short(“level”),
    row.longOption(“trainer_id”)
  )
  (pokemon.name, pokemon)
}

SQL(“””
  SELECT *
  FROM professor_oaks_pokemon
  WHERE pokemon_type={type}
“””).on { implicit query =>
  string(“type”, “starter”)
}.executeQuery()(connection).asMap(nameToPokemonParser)
```

##### Single Column Parsers

Sometimes a query retrieves only one column. Convenience methods are defined in RowParser for creating single column row parsers in these occasions. Below is an example of their use:

```
SQL(“””
  SELECT id
  FROM trainers
  WHERE name=”Red”
“””).executeQuery()(connection).asList(RowParser.long(“id”))
```

The RowParser object also contains definitions for bigInt, date, int, and string.

##### Single Value Parsers

In other cases, only one value is desired as the result of a query. For these scenarios, Relate provides a scalar method with which the desired type of the returned single value can be defined. The return value is wrapped as an Option. An example of its use is as follows:

```
SQL(“””
  SELECT hp
  FROM pokemon
  WHERE name=”Squirtle”
“””).executeQuery().scalar[Int].get
```

##### Retrieving Auto Increment Values on Insert

The scalar method can be used to retrieve auto increment values. Given a table where the primary key was a long, here's an example:

```
SQL(“””
  INSERT INTO badges(name)
  VALUES (“Boulder Badge”)
“””).executeInsert().scalar[Long].get
```

### .expand – Efficient Parameter Insertion

Relate allows collections of data to be inserted into queries with the expand method. This method must be called before the on method and, like on, takes an anonymous function as a parameter. The anonymous function denotes how expand the original query.

##### .commaSeparated

To take advantage of the SQL IN clause, use the commaSeparated method. This method expands the query by creating a comma separated list. It takes two parameters:
 
1. The name of the parameter to expand
2. The size of the comma separated list

Later, when calling the on method, use the plural versions of the insertion methods (just add an 's') to insert the collection into the query. Here is an example of its use:

```
import com.lucidchart.open.relate._
import com.lucidchart.open.relate.Query._

val ids = (0 until 150 by 10).map { i => i.toLong }

SQL(“””
  SELECT *
  FROM pokedex
  WHERE id IN ({ids})
“””).expand { implicit query =>
  commaSeparated(“ids”, ids.size)
}.on { implicit query =>
  longs(“ids”, ids)
}
```

Once again, importing com.lucidchart.open.relate.Query._ and declaring the query object as implicit allows implicit parameter insertion method calls.

##### .tupled and .onTuples

To insert multiple records into a table with one query, use the tupled and onTuples methods. tupled should be called in the function passed to expand. It specifies the name of the parameter to be replaced with tuples and the number of tuples that will be inserted. The method takes three parameters:

1. The name of the parameter to expand
2. A collection that contains the parameter name for each position in the tuple
3. The number of tuples to insert

After the expand method has been called, the onTuples method inserts values for the tuples in the query. It takes three parameters:

1. The name of the parameter for which to insert values
2. A collection of tuple data
3. An anonymous function that inserts data into tuples

onTuples will iterate through the collection, passing each value into the anonymous function. The anonymous function is also provided with a tuple version of the query object that contains the same insertion methods as the regular query object. However, there is no way to implicitly call methods on the tuple query, so they must be called explicitly. Here's an example of how the process works:

```
case class Trainer(name: String, catchPhrase: String)

val route1Trainers = List(
  (“Youngster Jimmy”, “My Rattata loves berries.”),
  (“Lass Haley”, “If you make eye contact, you have to battle!”),
  (“Pokemon Breeder Rocco”, “Soar high, my little Pidgey!”)
)

SQL(“””
  INSERT INTO trainers
  VALUES {tuples}
“””).expand { implicit query =>
  tupled(“tuples”, List(“name”, “catchPhrase”), route1Trainers.size)
}.onTuples(“tuples”, route1Trainers) { (trainer, query) =>
  query.string(“name”, trainer.name)
  query.string(“catchPhrase”, trainer.catchPhrase)
}.executeUpdate()(connection)
```

### Parameter Insertion Methods

The following is a list of all parameter insertion methods. Each takes a parameter name and a value.

* bigDecimal
* bigDecimals
* bigDecimalOption

* bigInt
* bigInts
* bigIntOption

* bool
* bools
* boolOption

* byte
* bytes
* byteOption

* byteArray
* byteArrayOption

* char
* chars
* charOption

* date
* dates
* dateOption

* double
* doubles
* doubleOption

* float
* floats
* floatOption

* int
* ints
* intOption

* long
* longs
* longOption

* short
* shorts
* shortOption

* string
* strings
* stringOption

* timestamp
* timestamps
* timestampOption

* uuid
* uuids
* uuidOption

### Data Extraction Methods

The following is a list of methods for extracting data from a row in the result. Each takes the name of the column to extract as a parameter.

* string
* stringOption
* int
* intOption
* double
* doubleOption
* short
* shortOption
* byte
* byteOption
* bool
* boolOption
* long
* longOption
* bigInt
* bigIntOption
* bigDecimal
* bigDecimalOption
* javaBigInteger
* javaBigIntegerOption
* javaBigDecimal
* javaBigDecimalOption
* date
* dateOption
* byteArray
* byteArrayOption
* uuid
* uuidOption
* uuidFromString
* uuidFromStringOption
* enum
* enumOption

* strictArray
* strictArrayOption
* strictAsciiStream
* strictAsciiStreamOption
* strictBigDecimal
* strictBigDecimalOption
* strictBinaryStream
* strictBinaryStreamOption
* strictBlob
* strictBlobOption
* strictBoolean
* strictBooleanOption
* strictByte
* strictByteOption
* strictBytes
* strictBytesOption
* strictCharacterStream
* strictCharacterStreamOption
* strictClob
* strictClobOption
* strictDate
* strictDateOption
* strictDate
* strictDateOption
* strictDouble
* strictDoubleOption
* strictFloat
* strictFloatOption
* strictInt
* strictIntOption
* strictLong
* strictLongOption
* strictNCharacterStream
* strictNCharacterStreamOption
* strictNClob
* strictNClobOption
* strictNString
* strictNStringOption
* strictObject
* strictObjectOption
* strictObject
* strictObjectOption
* strictRef
* strictRefOption
* strictRowId
* strictRowIdOption
* strictShort
* strictShortOption
* strictSQLXML
* strictSQLXMLOption
* strictString
* strictStringOption
* strictTime
* strictTimeOption
* strictTime
* strictTimeOption
* strictTimestamp
* strictTimestampOption
* strictTimestamp
* strictTimestampOption
* strictURL
* strictURLOption
