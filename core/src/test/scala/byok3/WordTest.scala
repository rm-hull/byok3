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

import java.io.File

import byok3.data_structures.MachineState.OK
import byok3.data_structures.Source.USER_INPUT_DEVICE
import byok3.helpers.capturingOutput
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
      ctx = ctx.eval(s"hex include $file", USER_INPUT_DEVICE)
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