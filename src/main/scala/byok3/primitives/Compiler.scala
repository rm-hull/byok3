package byok3.primitives

import byok3.annonation.{Documentation, Immediate}
import byok3.data_structures.Context._
import byok3.data_structures.Dictionary.{add, addressOf, last}
import byok3.data_structures.Registers.postIncIP
import byok3.data_structures.Stack.push
import byok3.data_structures.{Context, OK, UserDefined}
import byok3.primitives.Memory.comma
import cats.data.StateT.{inspect, modify}
import cats.implicits._

import scala.util.Try

object Compiler {

  val `(LIT)` = for {
    ip <- register(postIncIP)
    _ <- dataStack(push(ip))
  } yield ()

  @Documentation("Enter compilation state and start the current definition, producing colon-sys", stackEffect = "( C: \"<spaces>name\" -- colon-sys )")
  val `:` = for {
    token <- nextToken()
    nest <- dictionary(addressOf("NEST"))
    addr <- comma(nest)
    _ <- modify[Try, Context](_.beginCompilation(token.value.toUpperCase, addr))
  } yield ()

  @Documentation("End the current definition, allow it to be found in the dictionary and enter interpretation state, consuming colon-sys", stackEffect = "( C: colon-sys -- )")
  @Immediate
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
    _ <- dictionary(add(lastWord.markImmediate))
  } yield ()
}
