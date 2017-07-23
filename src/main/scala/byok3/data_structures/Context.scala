package byok3.data_structures

import byok3.data_structures.Stack.Stack
import byok3.primitives.{Arithmetics, StackManip}
import cats.data.State
import cats.data.State._


case class Context(ds: Stack[Int], // data stack
                   rs: Stack[Int], // return stack
                   mem: Memory,
                   reg: Registers,
                   status: MachineState,
                   exeTok: Map[String, ExecutionToken])


object Context {

  val tmpExeTok = Map(
    "+" -> Primitive(Arithmetics.+),
    "-" -> Primitive(Arithmetics.-),
    "*" -> Primitive(Arithmetics.*),
    "/" -> Primitive(Arithmetics./),
    "DUP" -> Primitive(StackManip.dup),
    "SWAP" -> Primitive(StackManip.swap),
    "DEPTH" -> Primitive(StackManip.depth),
    "DROP" -> Primitive(StackManip.drop)
  )

  def apply(memSize: Int): Context = Context(Stack.empty, Stack.empty, Memory(memSize), Registers(), OK, tmpExeTok)

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
}
