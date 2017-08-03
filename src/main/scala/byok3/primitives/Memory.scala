package byok3.primitives

import byok3.annonation.{Documentation, StackEffect}
import byok3.data_structures.Context._
import byok3.data_structures.Dictionary._
import byok3.data_structures.Memory.{peek, poke}
import byok3.data_structures.Registers._
import byok3.data_structures.Stack.{pop, push}
import byok3.data_structures.{Constant, Error, Variable}
import byok3.types.{AppState, Data}
import cats.data.StateT._
import cats.implicits._

import scala.util.Failure

object Memory {

  def comma(value: Data) = for {
    addr <- register(postIncDP)
    _ <- memory(poke(addr, value))
  } yield addr

  @Documentation("Store x at a-addr.")
  @StackEffect("( x a-addr -- )")
  val `!` = for {
    addr <- dataStack(pop)
    data <- dataStack(pop)
    _ <- memory(poke(addr, data))
  } yield ()

  @Documentation("x is the value stored at a-addr.")
  @StackEffect("( a-addr -- x )")
  val `@` = for {
    addr <- dataStack(pop)
    data <- memory(peek(addr))
    _ <- dataStack(push(data))
  } yield ()

  @Documentation("Adds x to the single cell number at a-addr.")
  @StackEffect("( x a-addr -- )")
  val +! = for {
    addr <- dataStack(pop)
    data <- memory(peek(addr))
    _ <- memory(poke(addr, data + 1))
  } yield ()

  @Documentation("Reserve one cell of data space and store x in the cell.")
  @StackEffect("( x -- )")
  val `,` = for {
    data <- dataStack(pop)
    _ <- comma(data)
  } yield ()

  val `(LIT)` = for {
    ip <- register(postIncIP)
    _ <- dataStack(push(ip))
  } yield ()

  @Documentation(
    """
      | Skip leading space delimiters. Parse name delimited by a space.
      | Create a definition for name with the execution semantics:
      |
      |   `name Execution: ( -- a-addr )`.
      |
      | Reserve one cell of data space at an aligned address.
    """.stripMargin)
  @StackEffect("( \"<spaces>name\" -- )")
  val VARIABLE = for {
    addr <- comma(0)
    token <- nextToken()
    _ <- dictionary(add(Variable(token.value, addr)))
  } yield ()

  @Documentation(
    """
      | Skip leading space delimiters. Parse name delimitedby a space.
      | Create a definition for name with the execution semantics:
      |
      |   `name Execution: ( -- x )`,
      |
      | which places x on the stack.
    """.stripMargin)
  @StackEffect("( x \"<spaces>name\" -- )")
  val CONSTANT = for {
    value <- dataStack(pop)
    token <- nextToken()
    _ <- dictionary(add(Constant(token.value, value)))
  } yield ()

  @Documentation(
    """
      | Parse ccc delimited by the delimiter char. c-addr is the address
      | (within the input buffer) and u is the length of the parsed string.
      | If the parse area was empty, the resulting string has a zero length.
    """.stripMargin)
  @StackEffect("( char \"ccc<char>\" -- c-addr u )")
  val PARSE = for {
    tib <- register(inspect(_.tib))
    ascii <- dataStack(pop)
    token <- nextToken(delim = s"\\Q${ascii.toChar}\\E")
    len = if (token.exhausted) 0 else token.value.length
    _ <- dataStack(push(len))
    _ <- dataStack(push(tib + token.offset))
  } yield ()

  @Documentation("addr is the data-space pointer.")
  @StackEffect("( -- addr )")
  val DP = for {
    dp <- register(inspect(_.dp))
    _ <- dataStack(push(dp))
  } yield ()

  @Documentation("addr is the data-space pointer.")
  @StackEffect("( -- addr )")
  val HERE = DP

  val TIB = for {
    tib <- register(inspect(_.tib))
    _ <- dataStack(push(tib))
  } yield ()

  @StackEffect("( i*x -- )")
  val THROW: AppState[Unit] =
    dataStack(pop).flatMapF(err => Failure(Error(err)))

  val `?ERROR` = for {
    err <- dataStack(pop)
    cond <- dataStack(pop)
  } yield if (cond == 0) () else throw Error(err)
}
