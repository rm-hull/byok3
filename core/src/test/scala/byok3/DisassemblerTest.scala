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
import byok3.data_structures.Context
import byok3.helpers.capturingOutput
import cats.implicits._
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers

class DisassemblerTest extends AnyFunSuite with Matchers {

  private val ops = for {
    _ <- Interpreter(": SQR DUP * ;")
    _ <- Interpreter(": SQR 9 9 * ;")
  } yield ()

  private val emptyContext = Context(0x1000)
  private val ctx = ops.runS(emptyContext).get
  private val disassembler = new Disassembler(ctx)

  test("should print disassembly") {
    val expected =
      s"""${MID_GREY}00000124:  37 00 00 00  |7...|  ${YELLOW}${BOLD}: <unknown>${RESET}
         |${MID_GREY}00000128:  7F 00 00 00  |....|  DUP
         |${MID_GREY}0000012C:  02 00 00 00  |....|  *
         |${MID_GREY}00000130:  38 00 00 00  |8...|  EXIT
         |${MID_GREY}00000134:  37 00 00 00  |7...|  ${YELLOW}${BOLD}: SQR${RESET}
         |${MID_GREY}00000138:  5F 00 00 00  |_...|  (LIT)
         |${MID_GREY}0000013C:  09 00 00 00  |....|  9
         |${MID_GREY}00000140:  5F 00 00 00  |_...|  (LIT)
         |${MID_GREY}00000144:  09 00 00 00  |....|  9
         |${MID_GREY}00000148:  02 00 00 00  |....|  *
         |${MID_GREY}0000014C:  38 00 00 00  |8...|  EXIT
         |""".stripMargin

    val actual = capturingOutput(disassembler.print(0x0124, 0x2C))
    actual shouldEqual expected
  }
}
