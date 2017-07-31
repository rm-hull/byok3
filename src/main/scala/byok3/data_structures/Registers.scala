package byok3.data_structures

import byok3.types.Address
import cats.data.StateT
import cats.data.StateT._
import cats.implicits._

import scala.util.Try

case class Registers(tib: Address, dp: Address, ip: Address) {
  def incDP = copy(dp = dp + 1)
  def incIP = copy(ip = ip + 1)
}


object Registers {
  def apply(): Registers = Registers(tib = 0x0000, dp = 0x0100, ip = 0x2000)

  def postIncDP: StateT[Try, Registers, Address] = for {
    reg <- get[Try, Registers]
    _ <- set[Try, Registers](reg.incDP)
  } yield reg.dp

  def postIncIP: StateT[Try, Registers, Address] = for {
    reg <- get[Try, Registers]
    _ <- set[Try, Registers](reg.incIP)
  } yield reg.ip
}
