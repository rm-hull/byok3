package byok3.primitives

import byok3.data_structures.Context._
import byok3.data_structures.Memory.{peek, poke}
import byok3.data_structures.Stack.{pop, push}
import byok3.data_structures.{Address, Context, Data}
import cats.data.{State}

object Memory {

  val `!`: State[Context, Unit] = for {
    x <- dataStack(pop)
    addr <- dataStack(pop)
    _ <- memory(poke(Address(addr), Data(x)))
  } yield ()

  val `@` = for {
    addr <- dataStack(pop)
    x <- memory(peek(Address(addr)))
    _ <- dataStack(push(x.value))
  } yield ()

  val +! = for {
    addr <- dataStack(pop)
    x <- memory(peek(Address(addr)))
    _ <- memory(poke(Address(addr), Data(x.value + 1)))
  } yield ()
}
