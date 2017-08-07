package byok3

import byok3.data_structures.{Context, Error}
import byok3.types.AppState
import cats.implicits._

import scala.annotation.tailrec
import scala.util.{Failure, Success}

trait Executor {

  def step: AppState[Boolean]

  @tailrec
  final def run(ctx: Context): Context = {
    step.run(ctx) match {
      case Failure(ex: Throwable) => ctx.error(Error(ex))
      case Success((next, false)) => run(next)
      case Success((next, true)) => next
    }
  }
}
