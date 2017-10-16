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

package byok3.console

import byok3.AnsiColor._
import byok3.data_structures.MachineState.BYE
import byok3.data_structures.{Context, Error}
import byok3.{Banner, Interruptible}
import cats.effect.IO
import org.jline.reader.LineReader.Option._
import org.jline.reader.{EndOfFileException, LineReaderBuilder, UserInterruptException}
import org.jline.terminal.TerminalBuilder

import scala.annotation.tailrec
import scala.util.{Failure, Success, Try}

object REPL {

  private val wordCompleter = new WordCompleter
  private val upperCaseParser = new UpperCaseParser
  private val terminal = TerminalBuilder.terminal()
  private val lineReader = LineReaderBuilder.builder
    .terminal(terminal)
    .parser(upperCaseParser)
    .completer(wordCompleter)
    .build

  lineReader.setOpt(DISABLE_EVENT_EXPANSION)
  lineReader.setOpt(CASE_INSENSITIVE)
  lineReader.setOpt(GROUP)

  def main(args: Array[String]): Unit = {
    println(Banner())

    val ctx = Context(0x500000)
      .copy(rawConsoleInput = Some(TerminalRawInput(terminal)))
      .eval("include forth/system.fth")

    loop(read)(ctx)
    println("exiting...")
  }

  private def read(ctx: Context) = {
    IO {
      wordCompleter.setContext(ctx)
      val input = lineReader.readLine(ctx.prompt)
      Interruptible.trigger(() => println())
      Console.withOut(terminal.output) {
        Predef.print(MID_GREY)
      }
      input
    }
  }

  @tailrec
  private def loop(reader: Context => IO[String])(ctx: Context): Context = {
    val program: IO[Context] = for {
      in <- reader(ctx)
      next = ctx.eval(in)
    } yield next

    Try(program.unsafeRunSync) match {
      case Success(next) => if (next.status == Right(BYE)) next else loop(reader)(next)
      case Failure(ex: UserInterruptException) => loop(reader)(ctx.reset.error(Error(-28)))
      case Failure(ex: EndOfFileException) => ctx
      case Failure(ex) => throw ex
    }
  }
}
