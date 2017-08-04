package byok3

import byok3.data_structures.{Context, Error, OK, Smudge}
import byok3.repl.AnsiColor._
import byok3.repl.Banner
import cats.effect.IO
import cats.implicits._
import org.jline.reader.{EndOfFileException, LineReaderBuilder, UserInterruptException}
import org.jline.terminal.TerminalBuilder

import scala.annotation.tailrec
import scala.io.Source
import scala.util.{Failure, Success, Try}

object REPL {

  val terminal = TerminalBuilder.builder.name("byok3").build
  val lineReader = LineReaderBuilder.builder
    .terminal(terminal)
    .build;

  def main(args: Array[String]): Unit = {
    println(Banner())
    val emptyContext = Context(0x500000)
    loop(read)(emptyContext)
    println("exiting...")
  }

  private def stackDepthIndicator(ctx: Context) = "." * ctx.ds.length

  private def read(ctx: Context) = {
    val prompt = ctx.status match {
      case Smudge => s"${LIGHT_GREY}|  "
      case OK => s"  ${WHITE}${BOLD}ok${LIGHT_GREY}${stackDepthIndicator(ctx)}\n"
      case err: Error => s"${RED}${BOLD}Error ${err.errno}:${RESET} ${err.message}\n"
    }

    IO {
      lineReader.readLine(prompt)
    }
  }

  private def eval(ctx: Context)(text: String) =
    Interpreter(text).runS(ctx).get

  private def print(ctx: Context) =
    IO {
      Console.withOut(terminal.output) {
        Predef.print(MID_GREY)
        ctx.output.unsafeRunSync()
      }
    }

  @tailrec
  private def loop(reader: Context => IO[String])(ctx: Context): Context = {
    val program: IO[Context] = for {
      in <- reader(ctx)
      next = eval(ctx)(in)
      _ <- print(next)
    } yield next

    Try(program.unsafeRunSync) match {
      case Success(next) => loop(reader)(next)
      case Failure(ex: UserInterruptException) => loop(reader)(ctx.reset.updateState(Error(-28)))
      case Failure(ex: EndOfFileException) => ctx
      case Failure(ex) => throw ex
    }
  }

  private def load(filename: String, ctx: Context): Context = {
    val in = IO {
      Source.fromFile(filename).mkString
    }
    loop(_ => in)(ctx)
  }
}
