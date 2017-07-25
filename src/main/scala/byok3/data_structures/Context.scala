package byok3.data_structures

import byok3.types.{Dictionary, Stack}
import cats.data.State
import cats.data.State._


case class Context(ds: Stack[Int], // data stack
                   rs: Stack[Int], // return stack
                   mem: Memory,
                   reg: Registers,
                   status: MachineState,
                   dictionary: Dictionary,
                   currentXT: Option[ExecutionToken] = None)

object Context {

  def apply(memSize: Int): Context = Context(Stack.empty, Stack.empty, Memory(memSize), Registers(), OK, DictionaryBuilder())

  def dataStack[A](block: State[Stack[Int], A]): State[Context, A] = for {
    ctx <- get[Context]
    (stack, value) = block.run(ctx.ds).value
    _ <- set(ctx.copy(ds = stack))
  } yield value

  def returnStack[A](block: State[Stack[Int], A]): State[Context, A] = for {
    ctx <- get[Context]
    (stack, value) = block.run(ctx.rs).value
    _ <- set(ctx.copy(rs = stack))
  } yield value

  def memory[A](block: State[Memory, A]): State[Context, A] = for {
    ctx <- get[Context]
    (memory, value) = block.run(ctx.mem).value
    _ <- set(ctx.copy(mem = memory))
  } yield value

  def machineState(newStatus: MachineState): State[Context, Unit] =
    modify[Context](_.copy(status = newStatus))

  def setCurrentXT(token: Option[ExecutionToken] = None): State[Context, Unit] =
    modify[Context](_.copy(currentXT = token))
}
