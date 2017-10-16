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
      s"""${MID_GREY}00000124:  2F 00 00 00  |/...|  ${CYAN}${BOLD}: <unknown>${RESET}${MID_GREY}
         |${MID_GREY}00000128:  71 00 00 00  |q...|  DUP
         |${MID_GREY}0000012C:  02 00 00 00  |....|  *
         |${MID_GREY}00000130:  30 00 00 00  |0...|  EXIT
         |${MID_GREY}00000134:  2F 00 00 00  |/...|  ${CYAN}${BOLD}: SQR${RESET}${MID_GREY}
         |${MID_GREY}00000138:  54 00 00 00  |T...|  (LIT)
         |${MID_GREY}0000013C:  09 00 00 00  |....|  9
         |${MID_GREY}00000140:  54 00 00 00  |T...|  (LIT)
         |${MID_GREY}00000144:  09 00 00 00  |....|  9
         |${MID_GREY}00000148:  02 00 00 00  |....|  *
         |${MID_GREY}0000014C:  30 00 00 00  |0...|  EXIT
         |""".stripMargin

    val actual = capturingOutput(disassembler.print(0x0124, 0x2C))
    actual shouldEqual expected
  }
}
