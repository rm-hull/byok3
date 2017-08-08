package byok3

import byok3.data_structures.CoreMemory._
import byok3.data_structures.{Context, UserDefined}
import byok3.repl.AnsiColor._
import byok3.types.Address

import scala.Predef.{print => pr}

class Disassembler(ctx: Context) {

  val nest = ctx.dictionary.indexOf("__NEST").get
  val defns = ctx.dictionary.toMap.values.map {
    case UserDefined(name, address, _) => Some((address, name))
    case _ => None
  }.flatten.toMap

  def print(offset: Address, len: Int): Unit = {
    def prevInstr(addr: Address) = {
      if (addr > offset) {
        val data = ctx.mem.peek(dec(addr))
        ctx.dictionary.get(data).map(_.name)
      }
      else None
    }

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

      val data = ctx.mem.peek(addr)
      val x = prevInstr(addr)
      x match {
        case Some("(LIT)") => pr(data)
        case Some("BRANCH") | Some("0BRANCH") => pr(f"$data ${YELLOW}(==> 0x${addr + data}%08X)${RESET}${MID_GREY}")
        case _ => {
          if (data == nest) {
            pr(s"${CYAN}${BOLD}${defns.get(addr).getOrElse("<unknown>")}: ${RESET}${MID_GREY}")
          }
          pr(ctx.dictionary.get(data).fold(data.toString)(_.name))
        }
      }
      pr("\n")
    }

    Range(offset, offset + len, CELL_SIZE).foreach(printRow)
  }
}