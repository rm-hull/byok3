package byok3.data_structures

import byok3.HexDump
import byok3.types.{Address, AddressSpace, Data}
import cats.data.StateT
import cats.data.StateT._
import cats.implicits._

import scala.annotation.tailrec
import scala.util.Try

case class CoreMemory(size: Int, private val addressSpace: AddressSpace) {

  import CoreMemory._

  private val empty: Data = 0

  private def boundsCheck(addr: Address): Unit = {
    if (addr < 0 || addr >= size)
      throw Error(-9, f"0x$addr%08X") // invalid memory address
  }

  private def alignmentCheck(addr: Address): Unit = {
    if (offset(addr) != 0)
      throw Error(-23, f"0x$addr%08X") // address alignment exception
  }

  def poke(addr: Address, data: Data): CoreMemory = {
    boundsCheck(addr)
    alignmentCheck(addr)
    copy(addressSpace = addressSpace.updated(addr, data))
  }

  def peek(addr: Address): Data = {
    boundsCheck(addr)
    alignmentCheck(addr)
    addressSpace.getOrElse(addr, empty)
  }

  def char_peek(addr: Address): Data = {
    boundsCheck(addr)
    val word = addressSpace.getOrElse(align(addr), empty)
    (word >> (offset(addr) << 3)) & 0xFF
  }

  def char_poke(addr: Address, data: Data): CoreMemory = {
    boundsCheck(addr)
    val alignedAddr = align(addr)
    val shifted = offset(addr) << 3
    val mask = 0xFF << shifted
    val newValue = (data & 0xFF) << shifted
    poke(alignedAddr, (peek(alignedAddr) & ~mask) | (newValue & mask))
  }

  @tailrec
  final def memcpy(addr: Address, data: String): CoreMemory = {
    data.headOption match {
      case Some(ch) => char_poke(addr, ch).memcpy(addr + 1, data.tail)
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
      else fetch0(addr + 1, len - 1, acc + char_peek(addr).toChar)
    }

    fetch0(addr, len, "")
  }

  lazy val hexDump = new HexDump(this)
}

case object CoreMemory {

  val CELL_SIZE = 4

  def align(addr: Address) = addr - offset(addr)

  def offset(addr: Address) = addr % CELL_SIZE

  def inc(addr: Address) = addr + CELL_SIZE

  def apply(size: Int): CoreMemory = {
    require(size > 0)
    require(offset(size) == 0)
    CoreMemory(size = size, Map.empty)
  }

  def poke(addr: Address, data: Data): StateT[Try, CoreMemory, Unit] =
    modify(_.poke(addr, data))

  def peek(addr: Address): StateT[Try, CoreMemory, Data] =
    inspect(_.peek(addr))

  def copy(addr: Address, data: String): StateT[Try, CoreMemory, Unit] =
    modify(_.memcpy(addr, data))

  def fetch(addr: Address, len: Int): StateT[Try, CoreMemory, String] =
    inspect(_.fetch(addr, len))
}