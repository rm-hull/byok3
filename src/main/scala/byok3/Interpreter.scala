package byok3

import byok3.data_structures.Context._
import byok3.data_structures.MachineState._
import byok3.data_structures.Stack._
import byok3.data_structures._
import byok3.primitives.Compiler._
import byok3.types.{AppState, Word}
import cats.data.StateT._
import cats.implicits._

import scala.util.Try


/**
  * Outer interpreter
  */
object Interpreter extends Executor {

  private def toNumber(value: String, radix: Int) =
    Try(Integer.parseInt(value, radix)).getOrElse(throw Error(-13, value))

  private def pushNumber(token: Word) = for {
    status <- machineState
    base <- deref("BASE")
    n = toNumber(token, base)
    _ <- if (status == Smudge) literal(n) else dataStack(push(n))
  } yield ()

  private def runOrCompile(xt: ExecutionToken) = for {
    status <- machineState
    _ <- if (xt.immediate || status != Smudge) xt.run else xt.compile
  } yield ()

  private def assemble: AppState[Unit] =
    get[Try, Context].flatMap { ctx =>
      ctx.input match {
        case Token(token, _, _) =>
          ctx.find(token)
            .map(xt => runOrCompile(xt))
            .getOrElse(pushNumber(token))
        case _ => pure(())
      }
    }

  override def step: AppState[Boolean] = for {
    _ <- assemble
    token <- nextToken()
  } yield token == EndOfData

  def apply(text: String): AppState[Unit] = for {
    _ <- input(text)
    _ <- modify[Try, Context](_.reset)
    _ <- modify(run)
  } yield ()
}