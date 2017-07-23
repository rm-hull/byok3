package byok3.data_structures

import byok3.data_structures.Stack.Stack
import cats.data.State
import cats.data.State._


case class Context(ds: Stack[Int], // data stack
                   rs: Stack[Word], // return stack
                   mem: Memory,
                   reg: Registers,
                   status: MachineState)


object Context {
  def apply(memSize: Int): Context = Context(Stack.empty, Stack.empty, Memory(memSize), Registers(), OK)

  def dataStack[A](block: State[Stack[Int], A]): State[Context, A] = for {
    ctx <- get[Context]
    (stack, value) = block.run(ctx.ds).value
    _ <- set(ctx.copy(ds = stack))
  } yield value

  def returnStack[A](block: State[Stack[Word], A]): State[Context, A] = for {
    ctx <- get[Context]
    (stack, value) = block.run(ctx.rs).value
    _ <- set(ctx.copy(rs = stack))
  } yield value

  def memory[A](block: State[Memory, A]): State[Context, A] = for {
    ctx <- get[Context]
    (memory, value) = block.run(ctx.mem).value
    _ <- set[Context](ctx.copy(mem = memory))
  } yield value

  def machineState(newStatus: MachineState) = modify[Context] {
    ctx => ctx.copy(status = newStatus)
  }
}
