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

package byok3

import byok3.data_structures.CoreMemory
import byok3.types.Address

import scala.Predef.{print => pr}

class HexDump(mem: CoreMemory) {

  private val BYTES_PER_BLOCK = 8

  def print(offset: Address, len: Int, columns: Int = 2): Unit = {
    val bytesPerLine = columns * BYTES_PER_BLOCK
    val extraLine = if ((offset + len) % bytesPerLine == 0) 0 else bytesPerLine

    def align(addr: Address) = addr - (addr % bytesPerLine)

    def valid(addr: Address) = addr >= offset && addr < offset + len

    def printable(i: Int) = if (i >= 32 && i < 127) i.toChar.toString else "."

    def printRow(addr: Address)(block: Int => String) = {
      for (j <- Range(0, columns); i <- Range(0, BYTES_PER_BLOCK)) {
        pr(block(addr + (j * BYTES_PER_BLOCK) + i))
        if (i == BYTES_PER_BLOCK - 1 && j != columns - 1) pr(" ")
      }
    }

    val start = align(offset)
    val end = align(offset + len + extraLine)

    for (addr <- Range(start, end, bytesPerLine)) {
      pr(f"$addr%08X:  ")
      printRow(addr) { n => if (valid(n)) f"${mem.char_peek(n)}%02X " else "   " }
      pr(" |")
      printRow(addr) { n => if (valid(n)) printable(mem.char_peek(n)) else " " }
      pr("|\n")
    }
  }
}