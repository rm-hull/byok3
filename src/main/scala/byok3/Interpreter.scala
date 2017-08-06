package byok3

import byok3.data_structures.Context._
import byok3.data_structures.Stack._
import byok3.data_structures._
import byok3.primitives.Compiler._
import byok3.types.{AppState, Word}
import cats.data.StateT._
import cats.implicits._

import scala.annotation.tailrec
import scala.util.{Failure, Success, Try}


object Interpreter {

  private def toNumber(value: String, radix: Int) =
    Try(Integer.parseInt(value, radix)).getOrElse(throw Error(-13, value))

  private def pushNumber(status: MachineState, token: Word) = for {
    base <- deref("BASE")
    n = toNumber(token, base)
    _ <- if (status == Smudge) literal(n) else dataStack(push(n))
  } yield ()

  private def runOrCompile(status: MachineState, xt: ExecutionToken) =
    if (xt.immediate || status != Smudge) xt.run else xt.compile

  private def assemble: AppState[Unit] =
    get[Try, Context].flatMap { ctx =>
      val status = ctx.status
      ctx.input match {
        case Token(token, _, _) =>
          ctx.find(token)
            .map(xt => runOrCompile(status, xt))
            .getOrElse(pushNumber(status, token))
        case _ => pure(ctx)
      }
    }

  private def step: AppState[Boolean] = for {
    _ <- assemble
    token <- nextToken()
  } yield token == EndOfData

  @tailrec
  private def exec(ctx: Context): Context = {
    step.run(ctx) match {
      case Success((next, false)) => exec(next)
      case Success((next, true)) => next
      case Failure(ex: Throwable) => ctx.updateState(Error(ex))
    }
  }

  def apply(text: String): AppState[Unit] = for {
    _ <- input(text)
    _ <- modify[Try, Context](_.reset)
    _ <- modify(exec)
  } yield ()
}