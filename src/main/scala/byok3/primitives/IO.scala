package byok3.primitives

import java.time.LocalDate

import byok3.Disassembler
import byok3.annonation.Documentation
import byok3.data_structures.{Context, Error}
import byok3.data_structures.Context._
import byok3.data_structures.CoreMemory._
import byok3.data_structures.Stack.{pop, push}
import byok3.types.{AppState, Stack}
import cats.data.StateT
import cats.data.StateT._
import cats.implicits._

import scala.util.Try

object IO {

  private def unsafeIO[A](block: => A): AppState[A] =
    StateT(s => Try((s, block)))

  private def num(base: Int)(n: Int) =
    BigInt(n).toString(base)

  @Documentation("convert signed number n to string of digits, and output", stackEffect = "( n -- )")
  val `.` = for {
    base <- deref("BASE")
    n <- dataStack(pop)
    _ <- unsafeIO {
      print(num(base)(n) + " ")
    }
  } yield ()

  @Documentation("display stack contents", stackEffect = "( -- )")
  val `.S` = for {
    base <- deref("BASE")
    stack <- dataStack(get[Try, Stack[Int]])
    _ <- unsafeIO {
      print(stack.reverse.map(num(base)).mkString(" ") + " ")
    }
  } yield ()

  @Documentation("outputs ascii as character", stackEffect = "( ascii -- )")
  val EMIT = for {
    ascii <- dataStack(pop)
    _ <- unsafeIO {
      print(ascii.toChar)
    }
  } yield ()

  @Documentation("waits for key, returns ascii", "( -- ascii )")
  val KEY = for {
    ctx <- get[Try, Context]
    ascii <- unsafeIO {
      val rawInput = ctx.rawConsoleInput.getOrElse(throw Error(-21))
      rawInput.read()
    }
    _ <- dataStack(push(ascii))
  } yield ()

  @Documentation("outputs u space characters", stackEffect = "( u -- )")
  val SPACES = for {
    n <- dataStack(pop)
    _ <- unsafeIO {
      print(" " * n)
    }
  } yield ()

  @Documentation("outputs the contents of addr for n bytes", stackEffect = "( addr n -- )")
  val TYPE = for {
    n <- dataStack(pop)
    addr <- dataStack(pop)
    data <- memory(fetch(addr, n))
    _ <- unsafeIO {
      print(data)
    }
  } yield ()

  @Documentation("displays the MIT license text", stackEffect = "( -- )")
  val LICENSE = unsafeIO {
    val now = LocalDate.now
    println(
      s"""|The MIT License (MIT)
          |
          |Copyright (c) ${now.getYear} Richard Hull
          |
          |Permission is hereby granted, free of charge, to any person obtaining a copy of
          |this software and associated documentation files (the "Software"), to deal in
          |the Software without restriction, including without limitation the rights to
          |use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
          |the Software, and to permit persons to whom the Software is furnished to do so,
          |subject to the following conditions:
          |
          |The above copyright notice and this permission notice shall be included in all
          |copies or substantial portions of the Software.
          |
          |THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
          |IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
          |FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
          |COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
          |IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
          |CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
       """.stripMargin)
    pure(())
  }

  @Documentation("List the definition names in alphabetical order", stackEffect = "( -- )")
  val WORDS = for {
    ctx <- get[Try, Context]
    dict = ctx.dictionary
    words = dict.toMap.filterNot(_._2.internal).keys.toList.sorted.mkString(" ")
    _ <- unsafeIO {
      println(words)
    }
  } yield ()

  @Documentation("Prints a hex dump of memory at the given address block", stackEffect = "( len a-addr -- )")
  val DUMP = for {
    len <- dataStack(pop)
    addr <- dataStack(pop)
    hexdump <- memory(inspect(_.hexDump))
    _ <- unsafeIO {
      hexdump.print(addr, len)
    }
  } yield ()

  @Documentation("Instruction disassembly at the given address block", stackEffect = "( len a-addr -- )")
  val DISASSEMBLE = for {
    len <- dataStack(pop)
    addr <- dataStack(pop)
    aligned = align(addr)
    disassembler <- inspect[Try, Context, Disassembler](_.disassembler)
    _ <- unsafeIO {
      disassembler.print(aligned, len)
    }
  } yield ()
}