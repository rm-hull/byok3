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

package byok3.primitives

import byok3.data_structures.Context
import byok3.data_structures.Context.{dataStack, exec}
import byok3.data_structures.Stack.push
import byok3.helpers.sequence
import byok3.types.AppState
import cats.data.StateT
import cats.implicits._
import org.scalatest.Assertion
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers

import scala.util.{Success, Try}

abstract class PrimitivesTestBase extends AnyFunSuite with Matchers {

  protected val emptyContext: Context = Context(0x10000)
  protected val DP: Int = exec("DP").runS(emptyContext).get.ds.head
  protected val IP: Int = exec("IP").runS(emptyContext).get.ds.head

  def assertDataStack(op: AppState[Unit], expected: List[Int],
                      presets: StateT[Try, Context, Unit] = sequence(dataStack(push(4)), dataStack(push(2)), dataStack(push(8)))): Assertion = {

    val effects = for {
      _ <- presets
      _ <- op
    } yield ()

    val result = effects.runS(emptyContext)
    result.map(_.ds) shouldEqual Success(expected)
  }
}
