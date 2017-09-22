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

import byok3.data_structures.CoreMemory._
import byok3.data_structures.{Context, UserDefined}
import byok3.console.AnsiColor._
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
      prevInstr(addr) match {
        case Some("(LIT)") => pr(data)
        case Some("BRANCH") | Some("0BRANCH") | Some("(LOOP)") => pr(f"$data ${YELLOW}(==> 0x${addr + data}%08X)${RESET}${MID_GREY}")
        case _ => pr(
          if (data == nest) s"${CYAN}${BOLD}: ${defns.get(addr).getOrElse("<unknown>")}${RESET}${MID_GREY}"
          else ctx.dictionary.get(data).fold(data.toString)(_.name))
      }
      pr("\n")
    }

    Range(offset, offset + len, CELL_SIZE).foreach(printRow)
  }
}