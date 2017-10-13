package byok3

import java.io.File

import byok3.data_structures.Context
import org.scalatest.{FunSuite, Matchers}


class WordTest extends FunSuite with Matchers {

  var ctx = Context(0x100000).eval("include forth/system.fth")

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

      println(res)
      ctx.error shouldBe None
    }
  }
}