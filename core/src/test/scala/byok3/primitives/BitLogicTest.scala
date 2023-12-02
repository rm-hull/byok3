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
import byok3.primitives.BitLogic._
import cats.implicits._

class BitLogicTest extends PrimitivesTestBase {

  private val ops = sequence(dataStack(push(3)), dataStack(push(19)))

  test("should bitwise-and top two stack elements") {
    assertDataStack(AND, List(3 & 19), presets = ops)
  }

  test("should bitwise-or top two stack elements") {
    assertDataStack(OR, List(3 | 19), presets = ops)
  }

  test("should bitwise-xor top two stack elements") {
    assertDataStack(XOR, List(3 ^ 19), presets = ops)
  }

  test("should bitwise-invert top stack element") {
    assertDataStack(INVERT, List(~19, 3), presets = ops)
  }

  test("should bitwise-shift-left top stack element by NOS bits") {
    assertDataStack(LSHIFT, List(3 << 19), presets = ops)
  }

  test("should bitwise-shift-right top stack element by NOS bits") {
    assertDataStack(RSHIFT, List(3 >> 19), presets = ops)
  }
}
