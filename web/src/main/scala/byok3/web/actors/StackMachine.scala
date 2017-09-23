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

package byok3.web.actors

import java.io.ByteArrayOutputStream

import akka.actor._
import byok3.AnsiColor._
import byok3.Banner
import byok3.data_structures.Context
import byok3.data_structures.MachineState.{OK, Smudge}
import cats.effect.IO

import scala.annotation.tailrec
import scala.io.Source
import scala.util.{Failure, Success, Try}


object StackMachine {
  def props(name: String) = Props(new StackMachine(name))
}

case class EndOfFileException() extends Exception

class StackMachine(name: String) extends Actor with ActorLogging {

  var ctx: Context = null
  var output: String = ""

  override def preStart() = {
    log.info(s"Starting: $name")

    output += capturingOutput {
      println(Banner())

      val systemLibs = Seq("forth/system.fth")

      ctx = systemLibs.map(load)
        .reduce(_ andThen _)
        .apply(Context(0x50000))
        .copy(rawConsoleInput = None)
    }
  }

  override def receive = {
    case input: String => {
      log.info(s"$name: $input (sender = ${sender()})")

      output += capturingOutput {
        ctx = ctx.eval(input)

        val prompt = ctx.status match {
          case Right(Smudge) => s"${LIGHT_GREY}|  "
          case Right(OK) => s"  ${WHITE}${BOLD}ok${LIGHT_GREY}${ctx.stackDepthIndicator}\n"
          case Left(err) => s"${RED}${BOLD}Error ${err.errno}:${RESET} ${err.message}\n"
        }

        Predef.print(s"$MID_GREY$prompt")
      }

      sender() ! output
      output = ""
    }
  }

  private def capturingOutput(program: => Any): String = {
    val baos = new ByteArrayOutputStream
    try {
      Console.withOut(baos) {
        program
      }
      baos.toString
    } finally {
      baos.close
    }
  }

  @tailrec
  private def loop(reader: Context => IO[String])(ctx: Context): Context = {
    val program: IO[Context] = for {
      in <- reader(ctx)
      next = ctx.eval(in)
    } yield next

    Try(program.unsafeRunSync) match {
      case Success(next) => loop(reader)(next)
      case Failure(ex: EndOfFileException) => ctx
      case Failure(ex) => throw ex
    }
  }

  private def load(filename: String)(ctx: Context): Context = {

    val lines = Source.fromResource(filename)
      .getLines()
      .zip(Stream.from(1).toIterator)

    def read(ctx: Context): IO[String] = IO {
      if (lines.hasNext) {
        val (text, line) = lines.next()
        ctx.error.foreach { err =>
          println(s"${RED}${BOLD}Error ${err.errno}:${RESET} ${err.message} occurred in ${BOLD}$filename, line: ${line - 1}${RESET}")
          throw new EndOfFileException()
        }
        text
      }
      else throw new EndOfFileException()
    }

    loop(read)(ctx.include(filename))
  }
}
