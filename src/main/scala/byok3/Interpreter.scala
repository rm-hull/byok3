package byok3

import byok3.data_structures.Context._
import byok3.data_structures.Stack._
import byok3.data_structures.{Context, Error, ExecutionToken}
import byok3.helpers.sequence
import byok3.types.Word
import cats.data.State
import cats.data.State._

import scala.util.Try

object Interpreter {

  private val delimiters = "[ \t\n]"

  def tokenize(in: String): Stream[Word] =
    in.split(delimiters).toStream.filterNot(_.isEmpty)

  private def parseAsNumber(token: Word) =
    Try(token.toInt)
      .toOption
      .map(n => for {
        _ <- setCurrentXT(None)
        _ <- dataStack(push(n))
      } yield ())

  private def effect(exeTok: ExecutionToken) =
    for {
      _ <- setCurrentXT(Some(exeTok))
      _ <- exeTok.effect
    } yield ()

  private def lookup(token: Word)(ctx: Context) =
    ctx.dictionary
      .get(token)
      .map(effect)

  private def assemble(token: Word): State[Context, Unit] =
    get[Context].flatMap { ctx =>
      lookup(token)(ctx)
        .orElse(parseAsNumber(token))
        .getOrElse(machineState(Error(-13))) // word not found
    }

  def exec(in: String): State[Context, Unit] = {
    val ops = tokenize(in).map(assemble)

    for {
      _ <- sequence(ops: _*)
    } yield ()
  }
}
