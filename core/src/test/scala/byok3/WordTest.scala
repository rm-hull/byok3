package byok3

import java.io.File

import byok3.data_structures.Error
import byok3.data_structures.MachineState.OK
import cats.implicits._
import org.scalatest.{FunSuite, Matchers}

import scala.util.{Failure, Success}

class WordTest extends FunSuite with Matchers {

  var ctx = emptyContext

  test("Context should initialize correctly") {
    ctx.error shouldBe None
  }

  def walkFolder(path: String)(block: String => Unit): Unit = {
    val folder = new File(getClass.getResource(path).getPath)
    if (folder.exists && folder.isDirectory)
      folder.listFiles
        .sorted
        .toList
        .foreach(f => if (f.isDirectory) walkFolder(s"$path/${f.getName}")(block) else block(s"$path/${f.getName}"))
  }

  walkFolder("/forth") { file =>
    test(s"${new File(file).getName}") {
      val res = capturingOutput {
        ctx = ctx.eval(s"include ${file.substring(1)}")
      }

      if (res.contains("INCORRECT RESULT:") || res.contains("WRONG NUMBER OF RESULTS:"))
        fail(res)

      if (res.trim.nonEmpty)
        println(res.trim)

      ctx.status shouldBe Right(OK)
    }
  }
}