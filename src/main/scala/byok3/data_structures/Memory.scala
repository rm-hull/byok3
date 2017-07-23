package byok3.data_structures

import byok3.StackMachineException._
import byok3.data_structures.Memory.AddressSpace
import cats.data.State
import cats.data.State._

protected case class Memory(size: Int, private val mem: AddressSpace) {

  private val empty = Data(0)

  private def boundsCheck(addr: Address): Unit = {
    if (addr.value < 0 || addr.value >= size)
      error(-9) // invalid memory address
  }

  def poke(addr: Address, data: Word) = {
    boundsCheck(addr)
    copy(mem = mem.updated(addr, data))
  }

  def peek(addr: Address): Word = {
    boundsCheck(addr)
    mem.getOrElse(addr, empty)
  }
}

case object Memory {

  type AddressSpace = Map[Address, Word]

  def apply(size: Int): Memory = {
    require(size > 0)
    Memory(size = size, Map.empty)
  }

  def poke(addr: Address, data: Word): State[Memory, Unit] =
    modify(_.poke(addr, data))

  def peek(addr: Address): State[Memory, Word] =
    inspect(_.peek(addr))
}