package byok3.data_structures

import byok3.types.{Address, AddressSpace, Data}
import cats.data.State
import cats.data.State._

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

  def poke(addr: Address, data: Data): State[Memory, Unit] =
    modify(_.poke(addr, data))

  def peek(addr: Address): State[Memory, Data] =
    inspect(_.peek(addr))
}