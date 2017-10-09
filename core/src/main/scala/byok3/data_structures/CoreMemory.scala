/*
 * Copyright (c) 2017 Richard Hull
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package byok3.data_structures

import byok3.HexDump
import byok3.data_structures.CoreMemory._
import byok3.types.{Address, AddressSpace, Data}
import cats.data.StateT
import cats.data.StateT._
import cats.implicits._

import scala.annotation.tailrec
import scala.util.Try

case class CoreMemory(size: Int, private val addressSpace: AddressSpace) {

  private val empty: Data = 0

  private def boundsCheck(addr: Address): Unit = {
    if (addr < 0 || addr >= size)
      throw Error(-9, f"0x$addr%08X") // invalid memory address
  }

  private def alignmentCheck(addr: Address): Unit = {
    if (offset(addr) != 0)
      throw Error(-23, f"0x$addr%08X") // address alignment exception
  }

  private def floor(addr: Address) = addr - offset(addr)

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
    val word = addressSpace.getOrElse(floor(addr), empty)
    (word >> (offset(addr) << 3)) & 0xFF
  }

  def char_poke(addr: Address, data: Data): CoreMemory = {
    boundsCheck(addr)
    val alignedAddr = floor(addr)
    val shifted = offset(addr) << 3
    val mask = 0xFF << shifted
    val newValue = (data & 0xFF) << shifted
    poke(alignedAddr, (peek(alignedAddr) & ~mask) | (newValue & mask))
  }

  @tailrec
  final def memcpy(addr: Address, data: String): CoreMemory =
    data.headOption match {
      case Some(ch) => char_poke(addr, ch).memcpy(addr + 1, data.tail)
      case None => this
    }

  final def move(dest: Address, src: Address, len: Int): CoreMemory = {
    require(len >= 0)
    boundsCheck(src)
    boundsCheck(src + len)
    boundsCheck(dest)
    boundsCheck(dest + dec(len))

    if (dest < src) move_fwd(dest, src, len) else move_back(dest, src, len)
  }

  @tailrec
  private def move_fwd(dest: Address, src: Address, len: Int): CoreMemory =
    if (len <= 0) this
    else poke(dest, peek(src)).move_fwd(inc(dest), inc(src), dec(len))

  @tailrec
  private def move_back(dest: Address, src: Address, len: Int): CoreMemory =
    if (len < 0) this
    else poke(dest + len, peek(src + len)).move_back(dest, src, dec(len))

  final def char_move(dest: Address, src: Address, len: Int): CoreMemory = {
    require(len >= 0)
    boundsCheck(src)
    boundsCheck(src + len)
    boundsCheck(dest)
    boundsCheck(dest + dec(len))

    if (dest < src) cmove_fwd(dest, src, len) else cmove_back(dest, src, len)
  }

  @tailrec
  private def cmove_fwd(dest: Address, src: Address, len: Int): CoreMemory =
    if (len <= 0) this
    else char_poke(dest, char_peek(src)).cmove_fwd(dest + 1, src + 1, len - 1)

  @tailrec
  private def cmove_back(dest: Address, src: Address, len: Int): CoreMemory =
    if (len < 0) this
    else char_poke(dest + len, char_peek(src + len)).cmove_back(dest, src, len - 1)

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

  def cfetch(caddr: Address): String =
    fetch(caddr + 1, char_peek(caddr))


  @tailrec
  final def char_fill(dest: Address, len: Int, char: Int): CoreMemory = {
    boundsCheck(dest)
    if (len > 0) char_poke(dest, char).char_fill(dest + 1, len -1, char)
    else this
  }

  lazy val hexDump = new HexDump(this)
}

case object CoreMemory {

  val CELL_SIZE = 4

  def align(addr: Address) = (addr + (CELL_SIZE - 1)) & ~(CELL_SIZE - 1)

  def offset(addr: Address) = addr % CELL_SIZE

  def inc(addr: Address) = addr + CELL_SIZE

  def dec(addr: Address) = addr - CELL_SIZE

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

  def cfetch(caddr: Address): StateT[Try, CoreMemory, String] =
    inspect(_.cfetch(caddr))
}