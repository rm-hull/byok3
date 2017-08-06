package byok3.primitives

import byok3.annonation.Documentation
import byok3.data_structures
import byok3.data_structures.Context._
import byok3.data_structures.CoreMemory.{peek, poke, _}
import byok3.data_structures.Dictionary._
import byok3.data_structures.Stack.{pop, push}
import byok3.data_structures._
import byok3.types.{Address, Data}
import cats.data.StateT._
import cats.implicits._

import scala.util.Try

object Memory {

  private def align(addr: Address) = (addr + (CELL_SIZE - 1)) & ~(CELL_SIZE - 1)

  def comma(value: Data) = for {
    addr <- register(inspect(_.dp))
    aligned = align(addr)
    _ <- memory(poke(aligned, value))
    _ <- register(modify(_.copy(dp = inc(aligned))))
  } yield aligned

  val CELL = Constant("CELL", CELL_SIZE)

  val `(LIT)` = for {
    ip <- register(inspect(_.ip))
    data <- memory(peek(ip))
    _ <- dataStack(push(data))
    _ <- register(modify(_.copy(ip = inc(ip))))
  } yield ()

  @Documentation("n2 is the size in address units of n1 cells", "( n1 -- n2 )")
  val CELLS = for {
    n1 <- dataStack(pop)
    n2 = if (n1 < 0) 0 else ((n1 - 1) / CELL_SIZE) + 1
    _ <- dataStack(push(n2))
  } yield ()

  @Documentation("Store x at a-addr", stackEffect = "( x a-addr -- )")
  val `!` = for {
    addr <- dataStack(pop)
    x <- dataStack(pop)
    _ <- memory(poke(addr, x))
  } yield ()

  @Documentation("x is the value stored at a-addr", stackEffect = "( a-addr -- x )")
  val `@` = for {
    addr <- dataStack(pop)
    x <- memory(peek(addr))
    _ <- dataStack(push(x))
  } yield ()

  @Documentation("Adds x to the single cell number at a-addr", stackEffect = "( x a-addr -- )")
  val +! = for {
    addr <- dataStack(pop)
    x <- dataStack(pop)
    data <- memory(peek(addr))
    _ <- memory(poke(addr, data + x))
  } yield ()

  @Documentation("Reserve one cell of data space and store x in the cell", stackEffect = "( x -- )")
  val `,` = for {
    x <- dataStack(pop)
    _ <- comma(x)
  } yield ()

  @Documentation("Store char at c-addr", stackEffect = "( char c-addr -- )")
  val `C!` = for {
    addr <- dataStack(pop)
    char <- dataStack(pop)
    _ <- memory(modify(_.char_poke(addr, char)))
  } yield ()

  @Documentation("Fetch the character stored at c-addr", stackEffect = "( c-addr -- x )")
  val `C@` = for {
    addr <- dataStack(pop)
    x <- memory(inspect(_.char_peek(addr)))
    _ <- dataStack(push(x))
  } yield ()

  @Documentation("Skip leading space delimiters. Parse name delimited by a space. Create a definition for name with the execution semantics: `name Execution: ( -- a-addr )`. Reserve one cell of data space at an aligned address", stackEffect = "( \"<spaces>name\" -- )")
  val VARIABLE = for {
    addr <- comma(0)
    token <- nextToken()
    _ <- dictionary(add(Variable(token.value.toUpperCase, addr)))
  } yield ()

  @Documentation("Skip leading space delimiters. Parse name delimitedby a space. Create a definition for name with the execution semantics: `name Execution: ( -- x )`, which places x on the stack", stackEffect = "( x \"<spaces>name\" -- )")
  val CONSTANT = for {
    value <- dataStack(pop)
    token <- nextToken()
    _ <- dictionary(add(Constant(token.value.toUpperCase, value)))
  } yield ()

  @Documentation("Parse ccc delimited by the delimiter char. c-addr is the address (within the input buffer) and u is the length of the parsed string. If the parse area was empty, the resulting string has a zero length", stackEffect = "( char \"ccc<char>\" -- c-addr u )")
  val PARSE = for {
    ascii <- dataStack(pop)
    tib <- deref("TIB")
    token <- nextToken(delim = s"\\Q${ascii.toChar}\\E")
    len = if (token.exhausted) 0 else token.value.length
    _ <- dataStack(push(tib + token.offset))
    _ <- dataStack(push(len))
    _ <- exec(">IN")
    tin <- dataStack(pop)
    _ <- memory(poke(tin, token.offset))
  } yield ()

  @Documentation("c-addr is the address of, and u is the number of characters in, the input buffer", stackEffect = "( -- c-addr u )")
  val SOURCE = for {
    tib <- deref("TIB")
    ctx <- get[Try, Context]
    len = ctx.input match {
      case EndOfData => 0
      case Token(_, _, in) => in.length
    }
    _ <- dataStack(push(tib))
    _ <- dataStack(push(len))
  } yield ()

  @Documentation("addr is the data-space pointer", stackEffect = "( -- addr )")
  val HERE = for {
    dp <- register(inspect(_.dp))
    _ <- dataStack(push(dp))
  } yield ()


}
