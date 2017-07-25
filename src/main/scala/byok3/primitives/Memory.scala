package byok3.primitives

import byok3.data_structures.Context
import byok3.data_structures.Context._
import byok3.data_structures.Memory.{peek, poke}
import byok3.data_structures.Stack.{pop, push}
import cats.data.State

object Memory {

  val `!`: State[Context, Unit] = for {
    data <- dataStack(pop)
    addr <- dataStack(pop)
    _ <- memory(poke(addr, data))
  } yield ()

  val `@` = for {
    addr <- dataStack(pop)
    data <- memory(peek(addr))
    _ <- dataStack(push(data))
  } yield ()

  val +! = for {
    addr <- dataStack(pop)
    data <- memory(peek(addr))
    _ <- memory(poke(addr, data + 1))
  } yield ()
}
