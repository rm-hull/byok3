/*
 * Copyright (c) 2017 Richard Hull
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package byok3.primitives

import byok3.annonation.Documentation
import byok3.data_structures.Context._
import byok3.data_structures.CoreMemory.{peek, poke, _}
import byok3.data_structures.Dictionary._
import byok3.data_structures.Stack.{pop, push}
import byok3.data_structures._
import byok3.types.Data
import cats.data.StateT._
import cats.implicits._

import scala.util.Try

object Memory {

  def comma(value: Data) = for {
    dp <- DP()
    aligned = align(dp)
    _ <- memory(poke(aligned, value))
    _ <- DP(inc(aligned))
  } yield aligned

  val `(LIT)` = for {
    ip <- IP()
    data <- memory(peek(ip))
    _ <- dataStack(push(data))
    _ <- IP(inc(ip))
  } yield ()

  @Documentation("n2 is the size in address units of n1 cells", stackEffect = "( n1 -- n2 )")
  val CELLS = for {
    n1 <- dataStack(pop)
    n2 = if (n1 < 0) 0 else n1 * CELL_SIZE
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

  @Documentation("", stackEffect = "( a1 a2 u --  )")
  val MOVE = for {
    u <- dataStack(pop)
    a2 <- dataStack(pop)
    a1 <- dataStack(pop)
    _ <- memory(modify(_.move(a2, a1, u * CELL_SIZE)))
  } yield ()

  @Documentation("", stackEffect = "( a1 a2 u --  )")
  val CMOVE = for {
    u <- dataStack(pop)
    a2 <- dataStack(pop)
    a1 <- dataStack(pop)
    _ <- memory(modify(_.char_move(a2, a1, u)))
  } yield ()

  @Documentation("If u is greater than zero, store char in each of u consecutive characters of memory beginning at c-addr", stackEffect = "( c-addr u char -- )")
  val FILL = for {
    char <- dataStack(pop)
    u <- dataStack(pop)
    addr <- dataStack(pop)
    _ <- memory(modify(_.char_fill(addr, u, char)))
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
    _ <- dataStack(push(tib + token.offset))
    _ <- dataStack(push(token.value.length))
    _ <- exec(">IN")
    tin <- dataStack(pop)
    _ <- memory(poke(tin, token.offset))
  } yield ()

  @Documentation("c-addr is the address of a transient region that can be used to hold data for intermediate processing", stackEffect = "( -- c-addr )")
  val PAD = for {
    dp <- DP()
    _ <- dataStack(push(dp + 128))
  } yield ()

  @Documentation("Skip leading delimiters. Parse characters ccc delimited by char. c-addr is the address of a transient region containing the parsed word as a counted string. If the parse area was empty or contained no characters other than the delimiter, the resulting string has a zero length. A program may replace characters within the string", stackEffect = "( char \"<chars>ccc<char>\" -- c-addr )")
  val WORD = for {
    _ <- PAD
    addr <- dataStack(pop)
    ascii <- dataStack(pop)
    token <- nextToken(delim = s"\\Q${ascii.toChar}\\E")
    _ <- dataStack(push(addr))
    _ <- memory(cstore(addr, token.value))
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
    dp <- DP()
    _ <- dataStack(push(dp))
  } yield ()

  @Documentation("Compare the string specified by c-addr1 u1 to the string specified by c-addr2 u2. The strings are compared, beginning at the given addresses, character by character, up to the length of the shorter string or until a difference is found. If the two strings are identical, n is zero. If the two strings are identical up to the length of the shorter string, n is minus-one (-1) if u1 is less than u2 and one (1) otherwise", stackEffect = "( c-addr1 u1 c-addr2 u2 -- n )")
  val COMPARE = for {
    u2 <- dataStack(pop)
    addr2 <- dataStack(pop)
    u1 <- dataStack(pop)
    addr1 <- dataStack(pop)

    word1 <- memory(fetch(addr1, u1))
    word2 <- memory(fetch(addr2, u2))

    result = word1.compare(word2)
    _ <- dataStack(push(result))
  } yield ()
}
