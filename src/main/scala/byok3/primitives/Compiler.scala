package byok3.primitives

import byok3.annonation.{Documentation, Immediate}
import byok3.data_structures.Context._
import byok3.data_structures.Dictionary.{add, addressOf, last, instruction}
import byok3.data_structures.Stack.{pop, push}
import byok3.data_structures._
import byok3.helpers._
import byok3.implicits._
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
    _ <- requires[Context](_.status != Smudge, Error(-29)) // compiler nesting
    token <- nextToken()
    nest <- dictionary(addressOf("__NEST"))
    addr <- comma(nest)
    _ <- modify[Try, Context](_.beginCompilation(token.value.toUpperCase, addr))
  } yield ()

  @Immediate
  @Documentation("End the current definition, allow it to be found in the dictionary and enter interpretation state, consuming colon-sys", stackEffect = "( C: colon-sys -- )")
  val `;` = for {
    unnest <- dictionary(addressOf("__UNNEST"))
    _ <- comma(unnest)
    userDefinedWord <- inspectF[Try, Context, UserDefined](_.compiling.toTry(Error(-14))) // used only during compilation
    _ <- dictionary(add(userDefinedWord))
    _ <- machineState(OK)
  } yield ()

  @Documentation("Make the most recent definition an immediate word", stackEffect = "( -- )")
  val IMMEDIATE = for {
    lastWord <- dictionary(last)
    _ <- dictionary(add(lastWord.markAsImmediate))
  } yield ()

  @Documentation("", stackEffect = "( -- xt )")
  val LATEST = for {
    xt <- dictionary(inspect(_.length - 1))
    _ <- dataStack(push(xt))
  } yield ()

  @Documentation("pfa is the parameter field address corresponding to xt", stackEffect = "( xt -- pfa )")
  val `>BODY` = for {
    xt <- dataStack(pop)
    instr <- dictionary(instruction(xt))
    pfa = instr match {
      case ud: UserDefined => ud.addr
      case _ => throw Error(-31)
    }
    _ <- dataStack(push(pfa))
  } yield ()
}
