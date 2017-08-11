import java.io.ByteArrayOutputStream

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
}
