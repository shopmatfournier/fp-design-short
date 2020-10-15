package mf

import scala.util.Try
import java.io.{BufferedInputStream, SequenceInputStream}

/*
 * INTRODUCTION
 *
 * In Functional Design, immutable values often model solutions to a problem,
 * and they are transformed and composed using _operators_.
 *
 * Operators come in two primary flavors: unary operators, which are the ones
 * that transform solutions into other solutions values with desired
 * properties; and binary operators, which combine two solutions for
 * subproblems into a solution for a larger problem.
 *
 * Composable operators accept and return similar types, which allows them
 * to be used repeatedly. For example, the `+` binary operator for integers
 * allows repeatedly adding numbers together (`1 + 2 + 3 + 4`) because the
 * return value of the operator is compatible with its input type.
 *
 * Composable operators allow you to generate a large variety of solutions out
 * of a smaller number of primitives, simply transforming and composing other
 * solutions.
 *
 * In this section, you'll see examples of composable operators on a variety
 * of immutable values, each of which models a solution to some problem.

 * In Functional Design, a functional domain consists of three things:
 *
 * 1. A set of types that model a solution to a domain problem.
 *
 * 2. Constructors that allow constructing simple solutions.
 *     these are the ADTs we covered in the past section.
 *
 * 3. Operators that solving more complex problems by transforming
 *    and combining solutions for subproblems

 * FILE I/O - EXERCISE SET 1
 *
 * Consider an ETL application that loads a lot of data from files and FTP
 * servers using Java's InputStream.
 */
object input_stream {
  import java.io.InputStream

  final case class IStream(createInputStream: () => InputStream) { self =>

    /**
     * EXERCISE 1
     *
     * Create an operator `++` that returns a new `IStream`, which will read
     * all data from the first input stream, and then when that one is
     * exhausted, it will close the first input stream, make the second
     * input stream, and continue reading from the second one.
     */
      // hint: use sequenceInputStream from java.io for simplicity sake
      // rolling your own by hand requires tempoary mutable variables and is a long solution
      // it's not the point of this exercise
    def ++(that: => IStream): IStream = ???

    /**
     * EXERCISE 2
     *
     * Create an operator `orElse` that returns a new `IStream`, which will
     * try to create the first input stream, but if that fails by throwing
     * an exception, it will then try to create the second input stream.
     */
    def orElse(that: => IStream): IStream = ???

    /**
     * EXERCISE 3
     *
     * Create an operator `buffered` that returns a new `IStream`, which will
     * create the input stream, but wrap it in Java's `BufferedInputStream`
     * before returning it.
     */
    def buffered: IStream = ???

  }

  object IStream {

    /**
     * Creates an empty stream.
     */
    val empty: IStream = IStream(() => new java.io.ByteArrayInputStream(Array.ofDim[Byte](0)))

    /**
     * Defers the construction of a `IStream` that might fail.
     * // delay evaluation so it blows up later.
     */
    def suspend(is: => IStream): IStream =
      IStream(() => is.createInputStream())
  }

  /**
   * EXERCISE 4
   *
   * Construct an IStream that will read from `primary`,
   * or will read from the concatenation of all `secondaries`,
   * and will buffer everything.
   */
  lazy val solution: IStream = ???
  lazy val primary: IStream           = ???
  lazy val secondaries: List[IStream] = ???
}

/**
 * EMAIL CLIENT - EXERCISE SET 2
 *
 * Consider a web email interface, which allows users to filter emails and
 * direct them to specific folders based on custom criteria.
 */
object email_filter {
  final case class Address(emailAddress: String)
  final case class Email(sender: Address, to: List[Address], subject: String, body: String)

  final case class EmailFilter(matches: Email => Boolean) { self =>

    /**
     * EXERCISE 1
     *
     * Add an "and" operator that will match an email if both the first and
     * the second email filter match the email.
     */
    def &&(that: EmailFilter): EmailFilter = ???
    /**
     * EXERCISE 2
     *
     * Add an "or" operator that will match an email if either the first or
     * the second email filter match the email.
     */
    def ||(that: EmailFilter): EmailFilter = ???

    /**
     * EXERCISE 3
     *
     * Add a "negate" operator that will match an email if this email filter
     * does NOT match an email.
     */
    def negate: EmailFilter = ???

    // or def unary_! : EmailFilter = ???

  }

  object EmailFilter {
    def senderIs(address: Address): EmailFilter = EmailFilter(_.sender == address)

    def recipientIs(address: Address): EmailFilter = EmailFilter(_.to.contains(address))

    def subjectContains(phrase: String): EmailFilter = EmailFilter(_.subject.contains(phrase))

    def bodyContains(phrase: String): EmailFilter = EmailFilter(_.body.contains(phrase))
  }

  /**
   * EXERCISE 4
   *
   * Make an email filter that looks for subjects that contain the word
   * "discount", bodies that contain the word "N95", and which are NOT
   * addressed to "john@doe.com". Build this filter up compositionally
   * by using the defined constructors and operators.
   */
  lazy val emailFilter1 = ???

}

/**
 * DATA TRANSFORM - EXERCISE SET 3
 *
 * Consider an email marketing platform, which allows users to upload contacts.
 */
object contact_processing {
  final case class SchemaCSV(columnNames: List[String]) {
    def relocate(i: Int, j: Int): Option[SchemaCSV] =
      if (i < columnNames.length && j < columnNames.length)
        Some(copy(columnNames = columnNames.updated(i, columnNames(j)).updated(j, columnNames(i))))
      else None

    def delete(i: Int): SchemaCSV = copy(columnNames = columnNames.take(i) ++ columnNames.drop(i + 1))

    def add(name: String): SchemaCSV = copy(columnNames = columnNames ++ List(name))
  }

  final case class ContactsCSV(schema: SchemaCSV, content: Vector[Vector[String]]) { self =>
    def get(column: String): Option[Vector[String]] =
      columnOf(column).map(i => content.map(row => row(i)))

    def add(columnName: String, column: Vector[String]): ContactsCSV =
      copy(schema = schema.add(columnName), content = content.zip(column).map { case (xs, x) => xs :+ x })

    def columnNames: List[String] = schema.columnNames

    def columnOf(name: String): Option[Int] = {
      val index = columnNames.indexOf(name)

      if (index >= 0) Some(index) else None
    }

    def get(row: Int, columnName: String): Option[String] =
      for {
        col   <- columnOf(columnName)
        row   <- content.lift(row)
        value <- row.lift(col)
      } yield value

    def rename(oldColumn: String, newColumn: String): ContactsCSV = {
      val index = schema.columnNames.indexOf(oldColumn)

      if (index < 0) self
      else copy(schema = SchemaCSV(schema.columnNames.updated(index, newColumn)))
    }

    def relocate(column: String, j: Int): Option[ContactsCSV] =
      columnOf(column).flatMap { i =>
        if (i < columnNames.length && j < columnNames.length)
          schema
            .relocate(i, j)
            .map(
              schema => copy(schema = schema, content = content.map(row => row.updated(j, row(i)).updated(i, row(j))))
            )
        else None
      }

    def delete(column: String): ContactsCSV =
      columnOf(column).map { i =>
        copy(schema = schema.delete(i), content = content.map(row => row.take(i) ++ row.drop(i + 1)))
      }.getOrElse(self)

    def combine(column1: String, column2: String)(
      newColumn: String
    )(f: (String, String) => String): Option[ContactsCSV] =
      for {
        index1 <- columnOf(column1)
        index2 <- columnOf(column2)
        column = content.map(row => f(row(index1), row(index2)))
      } yield add(newColumn, column).delete(column1).delete(column2)
  }

  // kind of like a Custom Either.
  sealed trait MappingResult[+A]
  object MappingResult {
    final case class Success[+A](warnings: List[String], value: A) extends MappingResult[A]
    final case class Failure(errors: List[String])                 extends MappingResult[Nothing]
  }

  final case class SchemaMapping(map: ContactsCSV => MappingResult[ContactsCSV]) { self =>
    import MappingResult._

    /**
     * EXERCISE 1
     *
     * Add a `+` operator that combines two schema mappings into one, applying
     * the effects of both in sequential order. If the first schema mapping
     * fails, then the result must fail. If the second schema mapping fails,
     * then the result must also fail. Only if both schema mappings succeed
     * can the resulting schema mapping succeed.
     *
     * e.g. we are turning an A into a B and then a B into a C basically.
     */
    def +(that: SchemaMapping): SchemaMapping = ???

    /**
     * EXERCISE 2
     *
     * Add an `orElse` operator that combines two schema mappings into one,
     * applying the effects of the first one, unless it fails, and in that
     * case, applying the effects of the second one.
     */
    def orElse(that: SchemaMapping): SchemaMapping = ???

  }

  object SchemaMapping {

    /**
     * EXERCISE 3
     *
     * Add a constructor for `SchemaMapping` that renames a column.
     */
    def rename(oldName: String, newName: String): SchemaMapping = ???

    /**
     * EXERCISE 4
     *
     * Add a constructor for `SchemaMapping` that combines two columns into one.
     */
    def combine(leftColumn: String, rightColumn: String)(newName: String)(
      f: (String, String) => String
    ): SchemaMapping = ???


    /**
     * EXERCISE 5
     *
     * Add a constructor for `SchemaMapping` that moves the column of the
     * specified name to the jth position.
     */
    def relocate(column: String, j: Int): SchemaMapping = ???


    /**
     * EXERCISE 6
     *
     * Add a constructor for `SchemaMapping` that deletes the column of the
     * specified name.
     */
      // not bothering with warnings if it doesn't exist for speed.
    def delete(name: String): SchemaMapping = ???
  }


  /**
   * EXERCISE 7
   *
   * Create a schema mapping that can remap the user's uploaded schema into the
   * company's official schema for contacts, by composing schema mappings
   * constructed from constructors and operators.
   */

  val UserUploadSchema: SchemaCSV =
    SchemaCSV(List("email", "fname", "lname", "country", "street", "postal"))

  val OfficialCompanySchema: SchemaCSV =
    SchemaCSV(List("full_name", "email_address", "country", "street_address", "postal_code"))

  lazy val schemaMapping: SchemaMapping = ???


}

// big reveal -> 