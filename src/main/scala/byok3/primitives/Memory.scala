package byok3.primitives

import byok3.data_structures.Context._
import byok3.data_structures.Memory.{peek, poke}
import byok3.data_structures.Stack.{pop, push}
import byok3.data_structures.{Context, PrimitiveImpl}
import cats.data.State

object Memory extends PrimitiveImpl {

  import scala.reflect.runtime.{universe => ru}
  override def typeOf = ru.typeOf[this.type]

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
