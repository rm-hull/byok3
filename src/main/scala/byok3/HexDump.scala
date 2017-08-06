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