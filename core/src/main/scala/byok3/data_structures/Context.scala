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

package byok3.data_structures

import byok3.AnsiColor._
import byok3.annonation.Documentation
import byok3.data_structures.Context._
import byok3.data_structures.CoreMemory._
import byok3.data_structures.Dictionary.{add, instruction}
import byok3.data_structures.MachineState._
import byok3.data_structures.Source._
import byok3.data_structures.Stack.pop
import byok3.primitives.IO.trace
import byok3.primitives.Memory.comma
import byok3.types._
import byok3.{Disassembler, Interpreter}
import cats.data.StateT
import cats.data.StateT._
import cats.implicits._

import scala.annotation.tailrec
import scala.collection.immutable.Stream.Empty
import scala.util.{Failure, Success, Try}


case class Context(mem: CoreMemory,
                   dictionary: Dict = Dictionary(),
                   error: Option[Error] = None,
                   position: Option[Position] = None,
                   input: Tokenizer = EndOfData,
                   ds: Stack[Int] = List.empty, // data stack
                   rs: Stack[Int] = List.empty, // return stack
                   compiling: Option[UserDefined] = None,
                   rawConsoleInput: Option[RawInput] = None,
                   included: Set[String] = Set.empty,
                   source: Source.Value = STRING) {

  def error(err: Error): Context =
  // reset the STATE to interpreter mode and then
  // drain the data and return stacks if there was an error
    machineState(OK).runS(this).get
      .copy(error = Some(err), ds = List.empty, rs = List.empty)

  def prompt = status match {
    case Right(Smudge) => s"${LIGHT_GREY}|  ${RESET}"
    case Right(BYE) => s"${LIGHT_GREY}> SYSTEM STOPPED${RESET}\n"
    case Right(OK) => s"  ${WHITE}${BOLD}ok${LIGHT_GREY}${stackDepthIndicator}${RESET}\n"
    case Left(err) => s"${RED}${BOLD}Error ${err.errno}:${RESET} ${err.getMessage}\n"
  }

  @deprecated("Candidate for removal", since = "14/10/2017") // TODO
  def find(token: Word) =
    dictionary.get(token.toUpperCase)

  def nextToken(delim: String) =
    copy(input = input.next(delim))

  def reset =
    copy(error = None, position = None)

  def status: Either[Error, MachineState.Value] = {
    @volatile lazy val state = machineState
      .runA(this)
      .toEither
      .left
      .map(Error(_, position))

    error match {
      case None => state
      case Some(err) => Left(Error(err, position))
    }
  }

  def beginCompilation(token: Word, addr: Address) =
    if (token.isEmpty) throw Error(-16) // attempt to use zero-length string as name
    else copy(compiling = Some(UserDefined(token, addr)))

  @volatile lazy val disassembler = new Disassembler(this)

  def eval(text: String, source: Source.Value): Context =
    Interpreter(text, source).runS(this) match {
      case Failure(ex) => error(Error(ex))
      case Success(ctx) => ctx
    }

  @tailrec
  private def load(lines: Stream[(String, Position)]): Context = lines match {
    case (line, position) #:: rest if error.isEmpty => eval(line, USER_INPUT_DEVICE).setPosition(position).load(rest)
    case Empty => reset
    case _ => this // not exhausted, possibly errored
  }

  private def setPosition(pos: Position) = copy(position = Some(pos))

  def include(filename: String, lines: Stream[(String, Position)]) =
    copy(included = included + filename).load(lines)

  def stackDepthIndicator = "." * math.min(16, ds.length)
}

object Context {

  private def bootstrap(dp: Address) = {
    for {
      // Can't initialise DP as init requires DP to already be set up:
      // poke the DP with the next cell's address
      _ <- memory(poke(dp, inc(dp)))
      _ <- dictionary(add(Constant("DP", dp)))
      _ <- dictionary(add(Constant("CELL", CELL_SIZE)))
      // Now set up the other core registers
      _ <- initialize("IP", 0)
      _ <- initialize("W", 0)
      _ <- initialize("XT", 0)
      _ <- initialize("STATE", 0)
      _ <- initialize("BASE", 10)
      // aux registers
      _ <- initialize("TIB", 0)
      _ <- initialize(">IN", 0, Some(Documentation("a-addr is the address of a cell containing the offset in characters from the start of the input buffer to the start of the parse area", stackEffect = "( -- a-addr )")))
      _ <- initialize("ECHO", 0)
    } yield ()
  }

  def apply(memSize: Int): Context = {
    require(offset(memSize) == 0)
    bootstrap(dp = 0x100).runS(Context(CoreMemory(memSize))).get
  }

  def initialize(name: Word, value: Data, doc: Option[Documentation] = None): AppState[Unit] = for {
    addr <- comma(value)
    _ <- dictionary(add(Constant(name, addr, doc)))
  } yield ()

  @inline
  def noOp: AppState[Unit] = pure(())

  @inline
  def requires[S](predicate: S => Boolean, onFail: Error): StateT[Try, S, Unit] =
    inspectF(s => if (predicate(s)) Success(()) else Failure(onFail))

  @inline
  def guard[S](predicate: => Boolean, onFail: Error): StateT[Try, S, Unit] =
    inspectF(_ => if (predicate) Success(()) else Failure(onFail))

  @inline
  def conditional[S](predicate: => Boolean, onTrue: StateT[Try, S, Unit]): StateT[Try, S, Unit] =
    if (predicate) onTrue else pure[Try, S, Unit](())

  def dataStack[A](block: StateT[Try, Stack[Int], A]): AppState[A] =
    block.transformS(_.ds, (ctx, stack) => ctx.copy(ds = stack))

  def returnStack[A](block: StateT[Try, Stack[Int], A]): AppState[A] =
    block.transformS(_.rs, (ctx, stack) => ctx.copy(rs = stack))

  def returnStackNotEmpty =
    requires[Context](_.rs.nonEmpty, Error(-6))

  def memory[A](block: StateT[Try, CoreMemory, A]): AppState[A] =
    block.transformS[Context](_.mem, (ctx, mem) => ctx.copy(mem = mem))

  def dictionary[A](block: StateT[Try, Dict, A]): AppState[A] =
    block.transformS[Context](_.dictionary, (ctx, dict) => ctx.copy(dictionary = dict))

  def machineState(newStatus: MachineState.Value): AppState[Unit] = for {
    _ <- exec("STATE")
    addr <- dataStack(pop)
    _ <- memory(poke(addr, newStatus.value))
  } yield ()

  def machineState: AppState[MachineState.Value] = for {
    state <- deref("STATE")
  } yield MachineState(state)

  def exec(token: Word): AppState[Unit] = for {
    xt <- dictionary(instruction(token))
    _ <- trace(xt.name)
    _ <- xt.effect
  } yield ()

  def deref(token: Word): AppState[Int] = for {
    xt <- dictionary(instruction(token))
    _ <- xt.effect
    addr <- dataStack(pop)
    ref <- memory(peek(addr))
  } yield ref

  def input(text: String): AppState[Boolean] = for {
    // TODO: check to make sure text.len < TIB size
    _ <- guard(text.length < 0x100, Error(-9, "input string too long"))
    tib <- deref("TIB")
    input = Tokenizer(text)
    _ <- modify[Try, Context](_.copy(input = input))
    _ <- memory(copy(tib, text))
    _ <- exec(">IN")
    tin <- dataStack(pop)
    _ <- memory(poke(tin, input.offset))
  } yield input == EndOfData

  def setSource(source: Source.Value): AppState[Unit] =
    modify(_.copy(source = source))

  def nextToken(delim: String = Tokenizer.delimiters): AppState[Tokenizer] = for {
    _ <- modify[Try, Context](_.nextToken(delim))
    ctx <- get[Try, Context]
  } yield ctx.input
}
