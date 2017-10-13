import java.io.ByteArrayOutputStream

import byok3.data_structures.Context
import org.scalatest.Matchers


package object byok3 extends Matchers {
  def capturingOutput(program: => Any): String = {
    val baos = new ByteArrayOutputStream
    try {
      Console.withOut(baos) { program }
      baos.toString
    } finally {
      baos.close
    }
  }

  val emptyContext = Context(0x100000).eval("include forth/system.fth")
}
