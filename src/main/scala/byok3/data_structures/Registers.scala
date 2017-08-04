package byok3.data_structures

import byok3.types.Address
import cats.data.StateT
import cats.data.StateT._
import cats.implicits._

import scala.util.Try

case class Registers(dp: Address = 0x100, ip: Address = 0x2000) {
  def incDP = copy(dp = dp + 1)
  def incIP = copy(ip = ip + 1)
}


object Registers {

  def postIncDP: StateT[Try, Registers, Address] = for {
    reg <- get[Try, Registers]
    _ <- set[Try, Registers](reg.incDP)
  } yield reg.dp

  def postIncIP: StateT[Try, Registers, Address] = for {
    reg <- get[Try, Registers]
    _ <- set[Try, Registers](reg.incIP)
  } yield reg.ip

  def setIP(addr: Address): StateT[Try, Registers, Unit] =
    modify[Try, Registers](_.copy(ip = addr))
}
