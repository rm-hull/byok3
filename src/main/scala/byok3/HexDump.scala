package byok3

import byok3.data_structures.Memory
import byok3.types.Address

import scala.Predef.{print => pr}

class HexDump(mem: Memory) {

  private val BYTES_PER_BLOCK = 8

  def print(offset: Address, len: Int, columns: Int = 2): Unit = {
    val bytesPerLine = columns * BYTES_PER_BLOCK
    val extraLine = if ((offset + len) % bytesPerLine == 0) 0 else bytesPerLine

    def align(addr: Address) = addr - (addr % bytesPerLine)

    def valid(addr: Address) = addr >= offset && addr < offset + len

    def printable(i: Int) = if (i >= 32 && i <= 127) i.toChar.toString else "."

    def printRow(addr: Address)(block: Int => Unit) = {
      Range(0, columns).foreach { j =>
        Range(0, BYTES_PER_BLOCK).foreach { i =>
          block(addr + (j * BYTES_PER_BLOCK) + i)
        }

        if (j != columns - 1) pr(" ")
      }
    }

    val start = align(offset)
    val end = align(offset + len + extraLine)

    Range(start, end, bytesPerLine).foreach { addr =>
      pr(f"$addr%08X:  ")
      printRow(addr) { n =>
        if (valid(n)) pr(f"${mem.peek(n)}%02X ")
        else pr("   ")
      }
      pr(" |")
      printRow(addr) { n =>
        if (valid(n)) pr(printable(mem.peek(n)))
        else pr(" ")
      }
      pr("|\n")
    }
  }
}