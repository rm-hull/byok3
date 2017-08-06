package byok3

import byok3.data_structures.Context
import byok3.data_structures.CoreMemory._
import byok3.types.Address

import scala.Predef.{print => pr}


class Disassembler(ctx: Context) {

  val lit = ctx.dictionary.indexOf("(LIT)").get

  def print(offset: Address, len: Int): Unit = {

    def printable(i: Int) = if (i >= 32 && i < 127) i.toChar.toString else "."

    def bytes(addr: Address)(block: Int => String) = {
      Range(0, CELL_SIZE).map(i => block(addr + i)).mkString("")
    }

    def printRow(addr: Address) = {
      pr(f"$addr%08X:  ")
      pr(bytes(addr) { n => f"${ctx.mem.char_peek(n)}%02X " })
      pr(" |")
      pr(bytes(addr) { n => printable(ctx.mem.char_peek(n)) })
      pr("|  ")

      if (addr > offset && ctx.mem.peek(addr - CELL_SIZE) == lit) {
        pr(ctx.mem.peek(addr))
      } else {
        val ptr = ctx.mem.peek(addr)
        pr(ctx.dictionary.get(ptr).fold(ptr.toString)(_.name))
      }
      pr("\n")
    }

    Range(offset, offset + len, CELL_SIZE).foreach(printRow)
  }
}