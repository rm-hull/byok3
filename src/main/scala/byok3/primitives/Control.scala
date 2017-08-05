package byok3.primitives

import byok3.annonation.{Documentation, Internal}
import byok3.data_structures.Context._
import byok3.data_structures.Dictionary._
import byok3.data_structures.Memory.peek
import byok3.data_structures.Registers._
import byok3.data_structures.Stack.{pop, push}
import byok3.data_structures.{Context, Error, ExecutionToken, Registers}
import byok3.implicits._
import byok3.types.{Address, AppState}
import cats.data.StateT._
import cats.implicits._

import scala.util.{Failure, Try}

object Control {

  @Internal
  val __NEST = for {
    addr <- register(inspect[Try, Registers, Address](_.ip))
    _ <- returnStack(push(addr))
    next = -1 // FIXME - this should be the address of the currentXT's param
    _ <- register(ip(next))
  } yield ()

  @Internal
  val __UNNEST = for {
    _ <- returnStackNotEmpty
    addr <- returnStack(pop)
    _ <- register(ip(addr))
  } yield ()

  @Internal
  val __EXEC = for {
    currXT <- inspectF[Try, Context, ExecutionToken](_.currentXT.toTry(Error(3)))

    opcode <- memory(peek(currXT.addr))
    exeTok <- dictionary(instruction(opcode))
    _ <- setCurrentXT(Some(exeTok))
    _ <- exec(exeTok.name)
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

}
