package byok3.data_structures

import byok3.Disassembler
import byok3.data_structures.Context._
import byok3.data_structures.CoreMemory.{copy, peek, poke}
import byok3.data_structures.Dictionary.add
import byok3.data_structures.MachineState.OK
import byok3.data_structures.Stack.pop
import byok3.primitives.Memory.comma
import byok3.types.{Address, AppState, Data, Dict, Stack, Word}
import cats.data.StateT
import cats.data.StateT._
import cats.effect.IO
import cats.implicits._

import scala.util.{Failure, Try}


case class Context(mem: CoreMemory,
                   dictionary: Dict = Dictionary(),
                   error: Option[Error] = None,
                   reg: Registers = Registers(),
                   input: Tokenizer = EndOfData,
                   output: IO[Unit] = IO.unit,
                   ds: Stack[Int] = List.empty, // data stack
                   rs: Stack[Int] = List.empty, // return stack
                   compiling: Option[UserDefined] = None) {

  def error(err: Error) =
    // reset the STATE to interpreter mode and then
    // drain the data and return stacks if there was an error
    machineState(OK).runS(this).get
      .copy(error = Some(err), ds = List.empty, rs = List.empty)

  def find(token: Word) =
    dictionary.get(token.toUpperCase)

  def nextToken(delim: String) =
    copy(input = input.next(delim))

  def append(out: IO[Unit]) =
    copy(output = output.flatMap(_ => out))

  def reset =
    copy(output = IO.unit, error = None)

  def exec(token: Word) =
    find(token).fold[Try[Context]](Failure(Error(-13, token))) {
      xt => xt.effect.runS(this)
    }

  def status: Either[Error, MachineState.Value] = {
    val state = machineState
      .runA(this)
      .toEither
      .left
      .map(Error(_))

    error match {
      case None => state
      case Some(err) => Left(err)
    }
  }

  def beginCompilation(token: Word, addr: Address) =
    if (token.isEmpty) throw Error(-16) // attempt to use zero-length string as name
    else copy(compiling = Some(UserDefined(token, addr)))

  lazy val disassembler = new Disassembler(this)
}

object Context {

  private val bootstrap = for {
    _ <- initialize("BASE", 10)
    _ <- initialize("TIB", 0)
    _ <- initialize(">IN", 0) // FIXME: add @Documentation("a-addr is the address of a cell containing the offset in characters from the start of the input buffer to the start of the parse area", stackEffect = "( -- a-addr )")
    _ <- initialize("ECHO", 0)
    _ <- initialize("STATE", 0)
  } yield ()

  def apply(memSize: Int): Context =
    bootstrap.runS(Context(CoreMemory(memSize))).get

  //  def dataStack2[A](block: StateT[Try, Stack[Int], A]): AppState[Unit] =
  //    modify[Try, Context](ctx => block.runS(ctx.ds).foldLeft[Context](ctx.updateState(Error(-4))) {
  //          (ctx, stack) => ctx.copy(ds = stack)
  //    })

  def initialize(name: Word, value: Data): AppState[Unit] = for {
    addr <- comma(value)
    _ <- dictionary(add(Constant(name, addr)))
  } yield ()

  def requires[S](predicate: S => Boolean, onFail: Error): StateT[Try, S, Unit] =
    inspectF[Try, S, Unit](s => Try(if (!predicate(s)) throw onFail))

  def guard[S](predicate: => Boolean, onFail: Error): StateT[Try, S, Unit] =
    inspectF[Try, S, Unit](_ => Try(if (!predicate) throw onFail))

  def dataStack[A](block: StateT[Try, Stack[Int], A]): AppState[A] =
    block.transformS(_.ds, (ctx, stack) => ctx.copy(ds = stack))

  def returnStack[A](block: StateT[Try, Stack[Int], A]): AppState[A] =
    block.transformS(_.rs, (ctx, stack) => ctx.copy(rs = stack))

  def returnStackNotEmpty =
    requires[Context](_.rs.nonEmpty, Error(-6))

  def memory[A](block: StateT[Try, CoreMemory, A]): AppState[A] =
    block.transformS[Context](_.mem, (ctx, mem) => ctx.copy(mem = mem))

  def register[A](block: StateT[Try, Registers, A]): AppState[A] =
    block.transformS[Context](_.reg, (ctx, reg) => ctx.copy(reg = reg))

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

  def exec(token: Word): AppState[Unit] =
    modifyF(_.exec(token))

  def deref(token: Word): AppState[Int] = for {
    _ <- exec(token)
    addr <- dataStack(pop)
    ref <- memory(peek(addr))
  } yield ref

  def input(text: String): AppState[Boolean] = for {
  // TODO: check to make sure text.len < TIB size
    _ <- guard(text.length < 0x100, Error(-9, "input string too long"))
    tib <- deref("TIB")
    _ <- modify[Try, Context](_.copy(input = Tokenizer(text)))
    _ <- memory(copy(tib, text))
    ctx <- get[Try, Context]
  } yield ctx.input == EndOfData

  def nextToken(delim: String = Tokenizer.delimiters): AppState[Tokenizer] = for {
    _ <- modify[Try, Context](_.nextToken(delim))
    ctx <- get[Try, Context]
  } yield ctx.input

  def output(block: => Unit): AppState[Unit] =
    modify(_.append(IO {
      block
    }))
}
