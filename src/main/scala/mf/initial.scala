package mf

import mf.ui_components.Turtle

import scala.concurrent.Future

/*
* In Functional Design, there are two ways to encode functional domain
* constructors and operators:
*
* 1. Using a function or interface, whose methods execute the solution. This is
*    called the "executable" encoding in this course. It's a direct, executable
*    encoding of a domain. If some functional domain is modeled with a class
*    or case class, or an open trait that is implemented by classes, then it's
*    probably an executable encoding. [FINAL ENCODING]
*
* 2. Using a pure data structure, which declaratively describes the solution, but
*    which does not perform the solution. It's an abstract, "declarative"
*    encoding of a domain. If some functional domain type is modeled with a
*    sealed trait, then it's probably an abstract encoding, where the subtypes
*    of the sealed trait model individual operations and constructors in the
*    domain. [INITIAL ENCODING]
*
* In the second encoding, a so-called "executor" or "interpreter" or "compiler"
* translates the data structure, which merely models a solution, into either
* executable code or into another lower-level domain, which provides the
* capabilities modeled by the functional domain.
*
* Executable encodings are "open": anyone can add new constructors and
* operators, without updating existing code. On the other hand, executable
* encodings are not "introspectable": because they are not data, but rather,
* opaque executable machinery, they cannot be serialized, optimized, or
* converted to other encodings.
*
* Abstract encodings are "introspectable": because they are pure data, they
* can be serialized, optimized, and converted to other encodings, assuming
* their component parts have the same properties (not all abstract encodings
* do; if you embed a function inside an abstract encoding, it's becomes
* opaque). On the other hand, abstract encodings are "closed": no one can add
* new constructors or operators, without updating existing code.
*
* Summarizing the difference between executable and abstract encodings:
*
*  - Executable encodings have open constructors/operators, but closed
*    interpreters.
*  - Declarative encodings have closed constructors/operators, but open
*    interpreters.
*
* Note: Tagless-final an executable encoding, but where by making the "solutions"
* polymorphic, the choice of executor can be deferred arbitrarily.
*
* Legacy code prefers executable encodings; while many benefits of Functional
* Design can be seen best using abstract encodings.

 * COMPOSABILITY - EXERCISE SET 2
 */
object ui_components {

  /**
   * EXERCISE 1
   *
   * The following API is not composable—there is no domain. Introduce a
   * domain with elements, constructors, and composable operators using an
   * initial encoding and a final encoding that uses this api.
   * .
   */
  trait Turtle { self =>
    def turnLeft(degrees: Int): Unit

    def turnRight(degrees: Int): Unit

    def goForward(): Unit

    def goBackward(): Unit

    def draw(): Unit
  }
 
  // exercise 1a , call it Turtle1.  Model it as a final encoding 

  // answer here

  // exercise 1b call it Turtle2. Model it as an initial encoding

  // answer here 
}

/**
 * DATA TRANSFORM - EXERCISE SET 2
 *
 * Consider an email marketing platform, which allows users to upload contacts.
 */
object contact_processing2 {
  import contact_processing._

  sealed trait SchemaMapping2 { self =>
    import SchemaMapping2._

    /**
     * EXERCISE 1
     *
     * Add a `+` operator that models combining two schema mappings into one,
     * applying the effects of both in sequential order.
     */
    def +(that: SchemaMapping2): SchemaMapping2 = ???

    /**
     * EXERCISE 2
     *
     * Add an `orElse` operator that models combining two schema mappings into
     * one, applying the effects of the first one, unless it fails, and in that
     * case, applying the effects of the second one.
     */
    def orElse(that: SchemaMapping2): SchemaMapping2 = ???
  }
  object SchemaMapping2 {
    // define parts here
    // for q1 and q2 q3 we need the ADT defined here for SchemaMapping2.


    /**
     * EXERCISE 3
     *
     * Add a constructor for `SchemaMapping` models renaming the column name.
     */
    def rename(oldName: String, newName: String): SchemaMapping2 = ???

    /**
     * EXERCISE 4
     *
     * Add a constructor for `SchemaMapping` that models deleting the column
     * of the specified name.
     */
    def delete(name: String): SchemaMapping2 = ???
  }

  /**
   * EXERCISE 5
   *
   * Implement an interpreter for the `SchemaMapping` model that translates it into
   * into changes on the contact list.
   */
  def run(mapping: SchemaMapping2, contacts: ContactsCSV): MappingResult[ContactsCSV] = ???

  /**
   * EXERCISE 6
   *
   * Implement an optimizer for the `SchemaMapping` model that detects and eliminates
   * redundant renames; e.g. renaming "name" to "first_name", and then back to "name".
   */
  def optimize(schemaMapping: SchemaMapping2): SchemaMapping2 = ???
}


// if we still have time GADTS

// we've been doing stuff with ADTS
// generalized ADT

// what it gives us ...

object Motivation2 {
  // we have some type for expressions we build
  sealed trait Expr { self =>
    def + (that: Expr): Expr = Expr.Add(self, that)
  }
  object Expr {
    case class ConstantInt(value: Int) extends Expr
    case class ConstantStr(value: String) extends Expr
    case class Add(left: Expr, right: Expr) extends Expr

    def int(v: Int): Expr = ConstantInt(v)

    Expr.int(2) + Expr.int(5) // and we'll get back an Expr

    // but the minute we add something that is NOT Int, we are screwed
    def eval(expr: Expr): Any =  // it's going to be Either Int, or String, so we're stuck with Any
    // and when we go in here to match against Add what do we do when Add is an Int and a String? blow up at runtime?
    // but we are in a typed language! we can detect this at compile time ...
      expr match {
        case Expr.Add(left, right) => ???
        case Expr.ConstantInt(v) => ???
        case Expr.ConstantStr(v) => ???
      }
  }
}

/**
 * EXPRESSIONS - EXERCISE SET 1
 *
 * Consider an application (such as the spreadsheet example) that needs to
 * calculate values in a user-defined way.
 *
 *
 * up until now everything was not generic (monomorphic), not a lot of type safety.
 */

// we want to leverage the compiler to do some bean counting for us to do stuff at runtime only

object Motivation {
  type Spreadsheet
  sealed trait Pipeline // we can do all sorts of things to it that are not well defined at runtime.

  //  final case class CalculatedValue(calculate: Spreadsheet => CellContents)
  /*
  we had a method called sum or + that did not make sense for all CalculatedValues
  // e.g. you could add a string to a double, which is obviously not something we want to do.
  // so our model was kind of brittle. Almost like it was dynamically typed
  // this is not good!
   */

  // but scala gives us a powerful type system that gives us lots of powerful tools
  // but you need to start introducing type parameters and getting comfortable with them
  sealed trait CellContents[A] {
    def calculate: Spreadsheet => A
  }
  object CellContents {
    final case class Dbl(value: Double) extends CellContents[Double] {
      def calculate = _ => value
    }
    final case class Str(value: String) extends CellContents[String] {
      def calculate = _ => value
    }
    final case class CalculatedValue[A](calculate: Spreadsheet => CellContents[A]) {
      // this sum method only works if [A] is the same, but how do we know we can sum them?
      def sum(that: CalculatedValue[A]): CalculatedValue[A] = ???

      def sum(that: CalculatedValue[A])(implicit aIsDouble: A <:< Double): CalculatedValue[A] = ???
    }
  }
}

// Just talk about this, don't explain it. 

object Gadt {
  sealed trait Expr[A] {
    self =>
    def +(that: Expr[A])(implicit aIsInt: A <:< Int): Expr[Int] =
      Expr.Add(self.as[Int], that.as[Int])

    def as[B](implicit ev: A <:< B): Expr[B] = Expr.As(self, ev)
  }

    object Expr {
      final case class ConstantInt(value: Int) extends Expr[Int]
      final case class ConstantStr(value: String) extends Expr[String]
      final case class As[A, B](expr: Expr[A], ev: A <:< B) extends Expr[B]
      final case class Add(left: Expr[Int], right: Expr[Int]) extends Expr[Int]

      def int(v: Int): Expr[Int] = ConstantInt(v)

      def str(v: String): Expr[String] = ConstantStr(v)

      def eval[A](expr: Expr[A]): A =
        expr match {
          case Expr.Add(left, right) => eval(left) + eval(right) // Int
          case Expr.ConstantInt(v) => v // Int
          case Expr.ConstantStr(v) => v // String
          case Expr.As(expr, ev) => ev(eval(expr)) // Sting or Int
        }
    }
  }


// Optional.  Also look at Task / Cats-effect IO map and flatMap methods under the hood. 

object recipes {
  sealed trait Baked[+A]
  object Baked {
    final case class Burnt[A](value: A)         extends Baked[A]
    final case class CookedPerfect[A](value: A) extends Baked[A]
    final case class Undercooked[A](value: A)   extends Baked[A]
  }

  sealed trait Ingredient
  object Ingredient {
    final case class Eggs(number: Int)        extends Ingredient
    final case class Sugar(amount: Double)    extends Ingredient
    final case class Flour(amount: Double)    extends Ingredient
    final case class Cinnamon(amount: Double) extends Ingredient
  }

  sealed trait Recipe[+A] { self =>

    /**
     * EXERCISE 1
     *
     * Implement a `map` operation that allows changing what a recipe produces.
     */
    def map[B](f: A => B): Recipe[B] = ???

    /**
     * EXERCISE 2
     *
     * Implement a `combine` operation that allows combining two recipes into
     * one, producing both items in a tuple.
     */
    def combine[B](that: Recipe[B]): Recipe[(A, B)] = ???

    /**
     * EXERCISE 3
     *
     * Implement a `tryOrElse` operation that allows trying a backup recipe,
     * in case this recipe ends in disaster.
     */
    def tryOrElse[B](that: Recipe[B]): Recipe[Either[A, B]] = ???

    /**
     * EXERCISE 4
     *
     * Implement a `flatMap` operation that allows deciding which recipe to
     * make after this recipe has produced its item.
     *
     * NOTE: Be sure to update the `make` method below so that you can make
     * recipes that use your new operation.
     */
    def flatMap[B](f: A => Recipe[B]): Recipe[B] = ???

    def bake(temp: Int, time: Int): Recipe[Baked[A]] = Recipe.Bake(self, temp, time)
  }
  object Recipe {
    case object Disaster                                              extends Recipe[Nothing]
    final case class AddIngredient(ingredient: Ingredient)            extends Recipe[Ingredient]
    final case class Bake[A](recipe: Recipe[A], temp: Int, time: Int) extends Recipe[Baked[A]]

    def addIngredient(ingredient: Ingredient): Recipe[Ingredient] = AddIngredient(ingredient)

    def disaster: Recipe[Nothing] = Disaster
  }
  import Recipe._

  def make[A](recipe: Recipe[A]): A =
    recipe match {
      case Disaster                  => throw new Exception("Uh no, utter disaster!")
      case AddIngredient(ingredient) => println(s"Adding ${ingredient}"); ingredient
      case Bake(recipe, temp, time) =>
        val a = make(recipe)

        println(s"Baking ${a} for ${time} minutes at ${temp} temperature")

        if (time * temp < 1000) Baked.Undercooked(a)
        else if (time * temp > 6000) Baked.Burnt(a)
        else Baked.CookedPerfect(a)
    }

  final case class Cake(ingredients: List[Ingredient])

  /**
   * EXERCISE 5
   *
   * Make a recipe that will produced a baked cake or other food of your choice!
   */
  lazy val recipe: Recipe[Baked[Cake]] = ???
}
