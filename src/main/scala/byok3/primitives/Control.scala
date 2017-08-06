package byok3.primitives

import byok3.annonation.{Documentation, Internal}
import byok3.data_structures.Context._
import byok3.data_structures.Dictionary._
import byok3.data_structures.Registers._
import byok3.data_structures.Stack.{pop, push}
import byok3.data_structures.{Error, Registers}
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
    // TODO consider an implicit pimp String -> Word that does is automatically
    name = if (token.value.isEmpty) throw Error(-32) else token.value.toUpperCase
    xt <- dictionary(addressOf(name))
    _ <- dataStack(push(xt))
  } yield ()

  @Documentation("Remove xt from the stack and perform the semantics identified by it. Other stack effects are due to the word EXECUTEd", stackEffect = "( i*x xt -- j*x )")
  val EXECUTE = for {
    addr <- dataStack(pop)
    xt <- dictionary(instruction(addr))
    _ <- setCurrentXT(Some(xt))
    _ <- exec(xt.name)
  } yield ()
}
