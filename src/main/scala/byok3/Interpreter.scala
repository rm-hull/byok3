package byok3

import byok3.data_structures.Context._
import byok3.data_structures.Stack._
import byok3.data_structures._
import byok3.types.{AppState, Word}
import cats.data.StateT
import cats.data.StateT._
import cats.free.Trampoline
import cats.implicits._

import scala.annotation.tailrec
import scala.util.{Failure, Success, Try}


object Interpreter {

  private def pushNumber(token: Word) =
    Try(token.toInt)
      .toOption
      .map(n => for {
        _ <- setCurrentXT(None)
        _ <- dataStack(push(n))
      } yield ())

  private def processEffect(token: Word)(ctx: Context) =
    ctx.dictionary
      .get(token)
      .map(xt => for {
        _ <- setCurrentXT(Some(xt))
        _ <- xt.effect
      } yield ())

  private def assemble: AppState[Unit] = get[Try, Context].flatMap { ctx =>
    ctx.input match {
      case EndOfData | Token("", _, _) => pure(ctx)
      case Token(token, _, _) =>
        processEffect(token)(ctx)
          .orElse(pushNumber(token))
          .getOrElse(machineState(Error(-13, token))) // word not found
    }
  }

  private def step: AppState[Boolean] = for {
    _ <- assemble
    token <- nextToken()
  } yield token == EndOfData

  @tailrec
  private def exec(ctx: Context): Context = {
    step.run(ctx) match {
      case Success((next, true)) => next
      case Success((next, false)) => exec(next)
      case Failure(ex) => throw ex // TODO: Should do something like: ctx.updateState(Error(-1, ex.getMessage))
    }
  }

  def apply(text: String): AppState[Unit] = for {
    _ <- input(text)
    _ <- modify(exec)
  } yield ()
}
