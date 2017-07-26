package byok3.data_structures

import byok3.types.{AppState, Dictionary, Stack}
import cats.data.StateT
import cats.data.StateT._
import cats.implicits._

import scala.util.Try


case class Context(mem: Memory,
                   dictionary: Dictionary,
                   status: MachineState = OK,
                   reg: Registers = Registers(),
                   ds: Stack[Int] = List.empty, // data stack
                   rs: Stack[Int] = List.empty, // return stack
                   currentXT: Option[ExecutionToken] = None) {

  def updateState(newStatus: MachineState) = newStatus match {
    // drain the data and return stacks if there was an error
    case err: Error => copy(status = err, ds = List.empty, rs = List.empty)
    case other => copy(status = other)
  }
}

object Context {

  def apply(memSize: Int): Context = Context(Memory(memSize), DictionaryBuilder())

  def dataStack[A](block: StateT[Try, Stack[Int], A]): AppState[A] =
    block.transformS(_.ds, (ctx, stack) => ctx.copy(ds = stack))

  def returnStack[A](block: StateT[Try, Stack[Int], A]): AppState[A] =
    block.transformS(_.rs, (ctx, stack) => ctx.copy(rs = stack))

  def memory[A](block: StateT[Try, Memory, A]): StateT[Try, Context, A] =
    block.transformS[Context](_.mem, (ctx, mem) => ctx.copy(mem = mem))

  def machineState(newStatus: MachineState): AppState[Unit] =
    modify(_.updateState(newStatus))

  def setCurrentXT(token: Option[ExecutionToken] = None): AppState[Unit] =
    modify[Try, Context](_.copy(currentXT = token))
}
