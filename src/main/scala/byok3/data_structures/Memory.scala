package byok3.data_structures

import byok3.types.{Address, AddressSpace, Data}
import cats.data.StateT
import cats.data.StateT._
import cats.implicits._

import scala.util.Try

protected case class Memory(size: Int, private val mem: AddressSpace) {

  private val empty: Data = 0

  private def boundsCheck(addr: Address): Unit = {
    if (addr < 0 || addr >= size)
      throw new IndexOutOfBoundsException(s"invalid memory address: $addr") // MError(-9) invalid memory address
  }

  def poke(addr: Address, data: Data) = {
    boundsCheck(addr)
    copy(mem = mem.updated(addr, data))
  }

  def peek(addr: Address): Data = {
    boundsCheck(addr)
    mem.getOrElse(addr, empty)
  }
}

case object Memory {

  def apply(size: Int): Memory = {
    require(size > 0)
    Memory(size = size, Map.empty)
  }

  def poke(addr: Address, data: Data): StateT[Try, Memory, Unit] =
    modify(_.poke(addr, data))

  def peek(addr: Address): StateT[Try, Memory, Data] =
    inspect[Try, Memory, Data](_.peek(addr))

  def copy(addr: Address, data: String): StateT[Try, Memory, Unit] = {
    Stream.from(addr).zip(data).foldLeft[StateT[Try, Memory, Unit]](pure()) {
      case (prevState, (addr, ch)) => for {
        _ <- prevState
        _ <- poke(addr, ch.toInt)
      } yield ()
    }
  }
}