package byok3.primitives

import byok3.annonation.{Documentation, Immediate}
import byok3.data_structures.Context._
import byok3.data_structures.Dictionary.{add, addressOf, last}
import byok3.data_structures.Stack.pop
import byok3.data_structures._
import byok3.helpers._
import byok3.primitives.Memory.comma
import cats.data.StateT._
import cats.implicits._

import scala.util.Try

object Compiler {

  def compile(ns: Int*) = sequence(ns.map(comma): _*)

  def literal(n: Int) = for {
    lit <- dictionary(addressOf("(LIT)"))
    _ <- compile(lit, n)
  } yield ()

  @Immediate
  @Documentation("Append the run-time semantics to the current definition.", "Compilation: ( x -- ), Runtime: ( -- x )")
  val LITERAL = for {
    _ <- requires[Context](_.status == Smudge, Error(-14)) // used only during compilation
    x <- dataStack(pop)
    _ <- literal(x)
  } yield ()

  @Documentation("Enter compilation state and start the current definition, producing colon-sys", stackEffect = "( C: \"<spaces>name\" -- colon-sys )")
  val `:` = for {
    token <- nextToken()
    nest <- dictionary(addressOf("NEST"))
    addr <- comma(nest)
    _ <- modify[Try, Context](_.beginCompilation(token.value.toUpperCase, addr))
  } yield ()

  @Immediate
  @Documentation("End the current definition, allow it to be found in the dictionary and enter interpretation state, consuming colon-sys", stackEffect = "( C: colon-sys -- )")
  val `;` = for {
    addr <- dictionary(addressOf("UNNEST"))
    _ <- comma(addr)
    userDefinedWord <- inspect[Try, Context, UserDefined](_.compiling.get)
    _ <- dictionary(add(userDefinedWord))
    _ <- machineState(OK)
  } yield ()

  @Documentation("Make the most recent definition an immediate word", stackEffect = "( -- )")
  val IMMEDIATE = for {
    lastWord <- dictionary(last)
    _ <- dictionary(add(lastWord.markAsImmediate))
  } yield ()
}
