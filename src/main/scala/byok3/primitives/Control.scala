package byok3.primitives

import byok3.annonation.{Documentation, Internal}
import byok3.data_structures.Context._
import byok3.data_structures.CoreMemory._
import byok3.data_structures.Dictionary._
import byok3.data_structures.Error
import byok3.data_structures.Registers._
import byok3.data_structures.Stack.{pop, push}
import byok3.types.AppState
import cats.data.StateT._
import cats.implicits._

import scala.util.Failure

object Control {

  @Internal
  val __NEST = for {
    addr <- register(inspect(_.ip))
    next <- register(inspect(_.w))
    _ <- register(ip(inc(next)))
    _ <- returnStack(push(addr))
  } yield ()

  @Internal
  val __UNNEST = for {
    _ <- returnStackNotEmpty
    addr <- returnStack(pop)
    _ <- register(ip(addr))
  } yield ()

  @Documentation(
    """
      | Return control to the calling definition specified by nest-sys. Before
      | executing EXIT within a do-loop, a program shall discard the loop-control
      | parameters by executing UNLOOP.
    """, stackEffect = "Execution: ( -- ) ( R: nest-sys -- )")
  val EXIT = __UNNEST

  @Documentation("TODO", stackEffect = "( i*x -- )")
  val THROW: AppState[Unit] =
    dataStack(pop).flatMapF[Unit](err => Failure(Error(err)))

  val `?ERROR` = for {
    err <- dataStack(pop)
    cond <- dataStack(pop)
  } yield if (cond == 0) () else throw Error(err)

  @Documentation("Skip leading space delimiters. Parse name delimited by a space. Find name and return xt, the execution token for name", stackEffect = "( \"<spaces>name\" -- xt )")
  val `'` = for {
    token <- nextToken()
    name = token.value.toUpperCase
    _ <- guard(name.nonEmpty, Error(-16))
    xt <- dictionary(addressOf(name))
    _ <- dataStack(push(xt))
  } yield ()

  @Documentation("Remove xt from the stack and perform the semantics identified by it. Other stack effects are due to the word EXECUTEd", stackEffect = "( i*x xt -- j*x )")
  val EXECUTE = for {
    xt <- dataStack(pop)
    instr <- dictionary(instruction(xt))
    _ <- exec(instr.name)
  } yield ()
}
