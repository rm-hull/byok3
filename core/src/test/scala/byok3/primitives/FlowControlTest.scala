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

import byok3.data_structures.Context._
import byok3.data_structures.Error
import byok3.data_structures.Stack._
import byok3.primitives.FlowControl._
import byok3.helpers.sequence
import cats.implicits._

class FlowControlTest extends PrimitivesTestBase {

  test("should throw an error") {
    val ops = sequence(dataStack(push(3)), dataStack(push(-3)), returnStack(push(9)), FlowControl.THROW)
    val ex = ops.runS(emptyContext).failed.get
    ex shouldEqual Error(-3)
  }

  test("should conditionally throw an error when NOS non-zero") {
    val ops = sequence(dataStack(push(3)), dataStack(push(-6)), `?ERROR`)
    val ex = ops.runS(emptyContext).failed.get
    ex shouldEqual Error(-6)
  }

  test("should not throw an error when NOS zero") {
    val ops = sequence(dataStack(push(0)), dataStack(push(-6)), `?ERROR`, dataStack(push(19)))
    val ctx = ops.runS(emptyContext).get
    ctx.ds shouldEqual List(19)
  }
}
