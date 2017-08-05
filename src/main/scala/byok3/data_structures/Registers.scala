package byok3.data_structures

import byok3.types.Address
import cats.data.StateT
import cats.data.StateT._
import cats.implicits._

import scala.util.Try

case class Registers(dp: Address = 0x100, ip: Address = 0x8000)


object Registers {

  def dp(addr: Address): StateT[Try, Registers, Unit] =
    modify[Try, Registers](_.copy(dp = addr))

  def ip(addr: Address): StateT[Try, Registers, Unit] =
    modify[Try, Registers](_.copy(ip = addr))
}
