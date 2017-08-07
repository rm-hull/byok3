package byok3.data_structures

import byok3.data_structures.Context._
import byok3.data_structures.CoreMemory._
import byok3.data_structures.Dictionary._
import byok3.types.{AppState, Data, Word}
import cats.implicits._


sealed abstract class Register(name: Word) {

  private def set(data: Data): AppState[Unit] = for {
    xt <- dictionary(instruction(name))
    addr = xt.asInstanceOf[Constant].value
    _ <- memory(poke(addr, data))
  } yield ()

  private def get: AppState[Data] = for {
    xt <- dictionary(instruction(name))
    addr = xt.asInstanceOf[Constant].value
    data <- memory(peek(addr))
  } yield data

  def apply() = get

  def apply(data: Data) = set(data)
}

object DP extends Register("DP")
object IP extends Register("IP")
object W extends Register("W")
object XT extends Register("XT")
