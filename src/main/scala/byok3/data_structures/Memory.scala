package byok3.data_structures

import byok3.HexDump
import byok3.types.{Address, AddressSpace, Data}
import cats.data.StateT
import cats.data.StateT._
import cats.implicits._

import scala.annotation.tailrec
import scala.util.Try

case class Memory(size: Int, private val mem: AddressSpace) {

  private val empty: Data = 0

  private def boundsCheck(addr: Address): Unit = {
    if (addr < 0 || addr >= size)
      throw new IndexOutOfBoundsException(f"0x$addr%08X")
  }

  def poke(addr: Address, data: Data): Memory = {
    boundsCheck(addr)
    copy(mem = mem.updated(addr, data))
  }

  def peek(addr: Address): Data = {
    boundsCheck(addr)
    mem.getOrElse(addr, empty)
  }

  @tailrec
  final def memcpy(addr: Address, data: String): Memory = {
    data.headOption match {
      case Some(ch) => poke(addr, ch).memcpy(addr + 1, data.tail)
      case None => this
    }
  }

  final def fetch(addr: Address, len: Int): String = {
    require(len >= 0)
    boundsCheck(addr)
    boundsCheck(addr + len)

    @tailrec
    def fetch0(addr: Address, len: Int, acc: String): String = {
      if (len == 0) acc
      else fetch0(addr + 1, len - 1, acc + peek(addr).toChar)
    }

    fetch0(addr, len, "")
  }

  lazy val hexDump = new HexDump(this)
}

case object Memory {

  def apply(size: Int): Memory = {
    require(size > 0)
    Memory(size = size, Map.empty)
  }

  def poke(addr: Address, data: Data): StateT[Try, Memory, Unit] =
    modify(_.poke(addr, data))

  def peek(addr: Address): StateT[Try, Memory, Data] =
    inspect(_.peek(addr))

  def copy(addr: Address, data: String): StateT[Try, Memory, Unit] =
    modify(_.memcpy(addr, data))

  def fetch(addr: Address, len: Int): StateT[Try, Memory, String] =
    inspect(_.fetch(addr, len))
}