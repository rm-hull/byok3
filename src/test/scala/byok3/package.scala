import java.io.ByteArrayOutputStream

import cats.effect.IO
import org.scalatest.Matchers


package object byok3 extends Matchers {
  def assertOutput(program: IO[Unit])(expected: String): Unit = {
    val baos = new ByteArrayOutputStream()
    Console.withOut(baos) {
      program.unsafeRunSync()
    }
    baos.toString shouldEqual expected
  }
}
