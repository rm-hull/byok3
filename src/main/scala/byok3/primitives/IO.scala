package byok3.primitives

import java.time.LocalDate

import biz.source_code.utils.RawConsoleInput
import byok3.Disassembler
import byok3.annonation.Documentation
import byok3.data_structures.Context
import byok3.data_structures.Context._
import byok3.data_structures.CoreMemory._
import byok3.data_structures.Stack.{pop, push}
import byok3.types.Stack
import cats.data.StateT._
import cats.implicits._
import org.jline.terminal.{Terminal, TerminalBuilder}

import scala.util.Try

object IO {

  private def num(base: Int)(n: Int) =
    BigInt(n).toString(base)

  @Documentation("convert signed number n to string of digits, and output", stackEffect = "( n -- )")
  val `.` = for {
    base <- deref("BASE")
    n <- dataStack(pop)
    _ <- performIO {
      print(num(base)(n) + " ")
      pure(())
    }
  } yield ()

  @Documentation("display stack contents", stackEffect = "( -- )")
  val `.S` = for {
    base <- deref("BASE")
    stack <- dataStack(get[Try, Stack[Int]])
    _ <- performIO {
      print(stack.reverse.map(num(base)).mkString(" ") + " ")
      pure(())
    }
  } yield ()

  @Documentation("outputs ascii as character", stackEffect = "( ascii -- )")
  val EMIT = for {
    ascii <- dataStack(pop)
    _ <- performIO {
      print(ascii.toChar)
      pure(())
    }
  } yield ()

  @Documentation("waits for key, returns ascii", "( -- ascii )")
  val KEY = performIO {
    val ascii = RawConsoleInput.read(true)
    dataStack(push(ascii))
  }

  @Documentation("outputs u space characters", stackEffect = "( u -- )")
  val SPACES = for {
    n <- dataStack(pop)
    _ <- performIO {
      print(" " * n)
      pure(())
    }
  } yield ()

  @Documentation("outputs the contents of addr for n bytes", stackEffect = "( addr n -- )")
  val TYPE = for {
    n <- dataStack(pop)
    addr <- dataStack(pop)
    data <- memory(fetch(addr, n))
    _ <- performIO {
      print(data)
      pure(())
    }
  } yield ()

  @Documentation("displays the MIT license text", stackEffect = "( -- )")
  val LICENSE = performIO {
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
    _ <- performIO {
      println(ctx.dictionary.toMap.filterNot(_._2.internal).keys.toList.sorted.mkString(" "))
      pure(())
    }
  } yield ()

  @Documentation("Prints a hex dump of memory at the given address block", stackEffect="( len a-addr -- )")
  val DUMP = for {
    len <- dataStack(pop)
    addr <- dataStack(pop)
    hexdump <- memory(inspect(_.hexDump))
    _ <- performIO {
      hexdump.print(addr, len)
      pure(())
    }
  } yield ()

  @Documentation("Instruction disassembly at the given address block", stackEffect = "( len a-addr -- )")
  val DISASSEMBLE = for {
    len <- dataStack(pop)
    addr <- dataStack(pop)
    aligned = align(addr)
    disassembler <- inspect[Try, Context, Disassembler](_.disassembler)
    _ <- performIO {
      disassembler.print(aligned, len)
      pure(())
    }
  } yield ()
}