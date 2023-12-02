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

import byok3.data_structures.Context.dataStack
import byok3.data_structures.Stack.push
import byok3.helpers.sequence
import cats.implicits._

class ArithmeticsTest extends PrimitivesTestBase {

  private val ops = sequence(dataStack(push(4)), dataStack(push(8)), dataStack(push(2)))

  test("should add top two stack elements") {
    assertDataStack(Arithmetics.+, List(10, 4), presets = ops)
  }
  test("should subtract top two stack elements") {
    assertDataStack(Arithmetics.-, List(6, 4), presets = ops)
  }
  test("should multiply top two stack elements") {
    assertDataStack(Arithmetics.*, List(16, 4), presets = ops)
  }
  test("should divide top two stack elements") {
    assertDataStack(Arithmetics./, List(4, 4), presets = ops)
  }
  test("should multiply and divide top three stack elements") {
    assertDataStack(Arithmetics.`*/`, List(16), presets = ops)
  }
  test("should multiply, divide and mod top three stack elements") {
    assertDataStack(Arithmetics.`*/MOD`, List(16, 16), presets = ops)
  }
  test("should divide and mod two three stack elements") {
    assertDataStack(Arithmetics.`/MOD`, List(4, 0, 4), presets = ops)
  }
  test("should negate the top stack element") {
    assertDataStack(Arithmetics.NEGATE, List(-2, 8, 4), presets = ops)
  }
  test("should abs the top stack element") {
    assertDataStack(Arithmetics.ABS, List(2), presets = sequence(dataStack(push(-2))))
  }
  test("should pick max for top two stack elements") {
    assertDataStack(Arithmetics.MAX, List(8, 4), presets = ops)
  }
  test("should pick min for top two stack elements") {
    assertDataStack(Arithmetics.MIN, List(2, 4), presets = ops)
  }
}