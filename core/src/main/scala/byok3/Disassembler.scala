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

import byok3.AnsiColor._
import byok3.data_structures.CoreMemory._
import byok3.data_structures.{Anonymous, Context, ForthWord}
import byok3.types.Address


class Disassembler(ctx: Context) {

  val nest = ctx.dictionary.indexOf("__NEST").get
  val defns = ctx.dictionary.toMap.values.map {
    case word: ForthWord => Some((word.addr, word.name))
    case anon: Anonymous => Some((anon.addr, anon.name))
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
      Predef.print(f"${MID_GREY}$addr%08X:  ")
      Predef.print(bytes(addr) { n => f"${ctx.mem.char_peek(n)}%02X " })
      Predef.print(" |")
      Predef.print(bytes(addr) { n => printable(ctx.mem.char_peek(n)) })
      Predef.print("|  ")

      val data = ctx.mem.peek(addr)
      val line = prevInstr(addr) match {
        case Some("(LIT)") => data
        case Some("BRANCH") | Some("0BRANCH") |
             Some("(LOOP)") | Some("(+LOOP)") |
             Some("(LEAVE)") => f"$data $YELLOW(==> 0x${addr + data}%08X)$RESET"
        case _ if (data == nest) => {
          val name = defns.get(addr).getOrElse("<unknown>")
          val immediate = ctx.dictionary.get(name).fold(false) { _.immediate }
          val position = ctx.dictionary.get(name).flatMap(_.position).map(pos => s" $pos").getOrElse("")
          s"$CYAN$BOLD: $name${if (immediate) s" $MAGENTA#IMMEDIATE" else ""}$RESET$position"
        }
        case _ => ctx.dictionary.get(data).fold(data.toString)(_.name)
      }
      Predef.println(line)
    }

    Range(offset, offset + len, CELL_SIZE).foreach(printRow)
  }
}