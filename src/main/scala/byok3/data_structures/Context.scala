package byok3.data_structures

import byok3.data_structures.Memory.{copy, peek}
import byok3.data_structures.Stack.{pop, push}
import byok3.types.{Address, AppState, Dict, Stack, Word}
import cats.data.StateT
import cats.data.StateT._
import cats.effect.IO
import cats.implicits._

import scala.util.Try


case class Context(mem: Memory,
                   dictionary: Dict = Dictionary(),
                   status: MachineState = OK,
                   reg: Registers = Registers(),
                   input: Tokenizer = EndOfData,
                   output: IO[Unit] = IO.unit,
                   ds: Stack[Int] = List.empty, // data stack
                   rs: Stack[Int] = List.empty, // return stack
                   currentXT: Option[ExecutionToken] = None) {

  def updateState(newStatus: MachineState) = newStatus match {
    // drain the data and return stacks if there was an error
    case err: Error => copy(status = err, ds = List.empty, rs = List.empty)
    case other => copy(status = other)
  }

  def nextToken(delim: String) = copy(input = input.next(delim))

  def append(out: IO[Unit]) = copy(output = output.flatMap(_ => out))

  def reset =
    copy(
      output = IO.unit,
      currentXT = None,
      status = if (status == Smudge) Smudge else OK)

  def getEffect(token: Word) =
    dictionary.get(token.toUpperCase)
      .map(_.effect)
      .getOrElse(throw Error(-13, token))

  def exec(token: Word) = getEffect(token).runS(this)
}

object Context {

  private val bootstrap = for {
    _ <- dataStack(push(10))
    _ <- exec("BASE")
    _ <- exec("!")
    _ <- dataStack(push(0x20))
    _ <- exec("TIB")
    _ <- exec("!")
  } yield ()

  def apply(memSize: Int): Context =
    bootstrap.runS(Context(Memory(memSize))).get

  //  def dataStack2[A](block: StateT[Try, Stack[Int], A]): AppState[Unit] =
  //    modify[Try, Context](ctx => block.runS(ctx.ds).foldLeft[Context](ctx.updateState(Error(-4))) {
  //          (ctx, stack) => ctx.copy(ds = stack)
  //    })


  def dataStack[A](block: StateT[Try, Stack[Int], A]): AppState[A] =
    block.transformS(_.ds, (ctx, stack) => ctx.copy(ds = stack))

  def returnStack[A](block: StateT[Try, Stack[Int], A]): AppState[A] =
    block.transformS(_.rs, (ctx, stack) => ctx.copy(rs = stack))

  def memory[A](block: StateT[Try, Memory, A]): AppState[A] =
    block.transformS[Context](_.mem, (ctx, mem) => ctx.copy(mem = mem))

  def register[A](block: StateT[Try, Registers, A]): AppState[A] =
    block.transformS[Context](_.reg, (ctx, reg) => ctx.copy(reg = reg))

  def dictionary[A](block: StateT[Try, Dict, A]): AppState[A] =
    block.transformS[Context](_.dictionary, (ctx, dict) => ctx.copy(dictionary = dict))

  def machineState(newStatus: MachineState): AppState[Unit] =
    modify(_.updateState(newStatus))

  def setCurrentXT(token: Option[ExecutionToken] = None): AppState[Unit] =
    modify(_.copy(currentXT = token))

  def exec(token: Word): AppState[Unit] =
    modifyF(_.exec(token))

  def deref(token: Word): AppState[Int] = for {
    _ <- exec(token)
    addr <- dataStack(pop)
    ref <- memory(peek(addr))
  } yield ref

  def input(text: String): AppState[Boolean] = for {
  // TODO: check to make sure text.len < TIB size
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
