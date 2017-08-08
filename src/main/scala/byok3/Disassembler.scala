package byok3

import byok3.data_structures.CoreMemory._
import byok3.data_structures.{Context, UserDefined}
import byok3.repl.AnsiColor._
import byok3.types.Address

import scala.Predef.{print => pr}

class Disassembler(ctx: Context) {

  val lit = ctx.dictionary.indexOf("(LIT)").get
  val nest = ctx.dictionary.indexOf("__NEST").get
  val defns = ctx.dictionary.toMap.values.map {
    case UserDefined(name, address, _) => Some((address, name))
    case _ => None
  }.flatten.toMap

  def print(offset: Address, len: Int): Unit = {

    def printable(i: Int) = if (i >= 32 && i < 127) i.toChar.toString else "."

    def bytes(addr: Address)(block: Int => String) = {
      Range(0, CELL_SIZE).map(i => block(addr + i)).mkString("")
    }

    def printRow(addr: Address) = {
      pr(f"${MID_GREY}$addr%08X:  ")
      pr(bytes(addr) { n => f"${ctx.mem.char_peek(n)}%02X " })
      pr(" |")
      pr(bytes(addr) { n => printable(ctx.mem.char_peek(n)) })
      pr("|  ")

      if (addr > offset && ctx.mem.peek(addr - CELL_SIZE) == lit) {
        pr(ctx.mem.peek(addr))
      } else {
        val ptr = ctx.mem.peek(addr)
        if (ptr == nest) {
          pr(s"${WHITE}${BOLD}${defns.get(addr).getOrElse("<unknown>")}: ${RESET}${MID_GREY}")
        }

        pr(ctx.dictionary.get(ptr).fold(ptr.toString)(_.name))
      }
      pr("\n")
    }

    Range(offset, offset + len, CELL_SIZE).foreach(printRow)
  }
}