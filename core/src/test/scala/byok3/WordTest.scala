package byok3

import java.io.File

import byok3.data_structures.MachineState.OK
import org.scalatest.{FunSuite, Matchers}

import scala.io.Source

class WordTest extends FunSuite with Matchers {

  var ctx = emptyContext

  test("Context should initialize correctly") {
    // make sure system.fth is in a sane place first
    ctx.error shouldBe None
  }

  walkFolder("/forth") { f =>
    val name = s"${new File(f).getName}"
    if (isMarkedAsIgnored(f)) ignore(name)(runForth(f)) else test(name)(runForth(f))
  }

  private def walkFolder(path: String)(block: String => Unit): Unit = {
    val folder = new File(getClass.getResource(path).getPath)
    if (folder.exists && folder.isDirectory)
      folder.listFiles
        .sorted
        .toList
        .foreach(f => if (f.isDirectory) walkFolder(s"$path/${f.getName}")(block) else block(s"$path/${f.getName}".substring(1)))
  }

  private def runForth(file: String) = {
    val res = capturingOutput {
      ctx = ctx.eval(s"hex include $file")
    }

    if (res.contains("INCORRECT RESULT:") || res.contains("WRONG NUMBER OF RESULTS:"))
      fail(res)

    if (res.trim.nonEmpty)
      println(res.trim)

    ctx.status shouldBe Right(OK)
  }

  private def isMarkedAsIgnored(file: String) =
    Source.fromResource(file).getLines().toStream.exists(_.toUpperCase.contains("@IGNORED"))
}