package byok3

import byok3.data_structures.Context
import AnsiColor._
import cats.implicits._
import org.scalatest.FunSuite

class DisassemblerTest extends FunSuite {

  val ops = for {
    _ <- Interpreter(": SQR DUP * ;")
    _ <- Interpreter(": SQR 9 9 * ;")
  } yield ()

  val emptyContext = Context(0x1000)
  val ctx = ops.runS(emptyContext).get
  val disassembler = new Disassembler(ctx)

  test("should print disassembly") {
    val expected =
      s"""${MID_GREY}00000124:  2B 00 00 00  |+...|  ${CYAN}${BOLD}: <unknown>${RESET}${MID_GREY}
         |${MID_GREY}00000128:  65 00 00 00  |e...|  DUP
         |${MID_GREY}0000012C:  02 00 00 00  |....|  *
         |${MID_GREY}00000130:  2C 00 00 00  |,...|  EXIT
         |${MID_GREY}00000134:  2B 00 00 00  |+...|  ${CYAN}${BOLD}: SQR${RESET}${MID_GREY}
         |${MID_GREY}00000138:  4A 00 00 00  |J...|  (LIT)
         |${MID_GREY}0000013C:  09 00 00 00  |....|  9
         |${MID_GREY}00000140:  4A 00 00 00  |J...|  (LIT)
         |${MID_GREY}00000144:  09 00 00 00  |....|  9
         |${MID_GREY}00000148:  02 00 00 00  |....|  *
         |${MID_GREY}0000014C:  2C 00 00 00  |,...|  EXIT
         |""".stripMargin

    val actual = capturingOutput(disassembler.print(0x0124, 0x2C))
    actual shouldEqual expected
  }
}