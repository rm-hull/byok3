package byok3.primitives

import byok3.data_structures.Context._
import byok3.data_structures.Memory.{peek, poke}
import byok3.data_structures.Stack.{pop, push}
import cats.implicits._

object Memory {

  val `!` = for {
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
