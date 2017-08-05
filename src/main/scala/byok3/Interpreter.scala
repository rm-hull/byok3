package byok3

import byok3.data_structures.Context._
import byok3.data_structures.Stack._
import byok3.data_structures._
import byok3.types.{AppState, Word}
import cats.data.StateT._
import cats.implicits._

import scala.annotation.tailrec
import scala.util.{Failure, Success, Try}


object Interpreter {

  private def toNumber(value: String, radix: Int) = try {
    Integer.parseInt(value, radix)
  } catch {
    case _: NumberFormatException => throw Error(-13, value)
  }

  private def pushNumber(token: Word) = for {
    base <- deref("BASE")
    _ <- setCurrentXT(None)
    n = toNumber(token, base)
    _ <- dataStack(push(n))
  } yield ()

  private def processEffect(token: Word)(ctx: Context) =
    ctx.dictionary
      .get(token.toUpperCase)
      .filterNot(_.internal)
      .map(xt => for {
        _ <- setCurrentXT(Some(xt))
        _ <- xt.effect
      } yield ())

  private def assemble: AppState[Unit] =
    get[Try, Context].flatMap { ctx =>
      ctx.input match {
        case Token(token, _, _) => processEffect(token)(ctx).getOrElse(pushNumber(token))
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