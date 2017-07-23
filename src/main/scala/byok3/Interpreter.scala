package byok3

import byok3.data_structures.Context._
import byok3.data_structures.Stack._
import byok3.data_structures.{Context, Error}
import cats.data.State
import cats.data.State._

import scala.util.Try

object Interpreter {

  private val delimiters = "[ \t\n]"

  private def tokenize(in: String): Stream[String] =
    in.split(delimiters).toStream

  private def parseAsNumber(token: String): State[Context, Unit] = {
    Try(token.toInt).fold(
      _ => machineState(Error),
      n => dataStack(push(n)))
  }

  private def process(token: String): State[Context, Unit] =
    get[Context].flatMap(ctx =>
      ctx.exeTok
        .get(token)
        .map(_.effect)
        .getOrElse(parseAsNumber(token)))

  def exec(in: String): State[Context, Unit] = {

    val ops = tokenize(in).map(process)

    for {
      _ <- sequence(ops: _*)
    } yield ()
  }
}
