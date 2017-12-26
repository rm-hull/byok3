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
import byok3.primitives.Comparison._
import cats.implicits._

class ComparisonTest extends PrimitivesTestBase {

  val ops = sequence(dataStack(push(3)), dataStack(push(19)))

  test("should compare top two stack elements for equality") {
    assertDataStack(`=`, List(0), presets = ops)
  }

  test("should compare top two stack elements for not equality") {
    assertDataStack(`<>`, List(-1), presets = ops)
  }

  test("should compare top two stack elements with less-than") {
    assertDataStack(Comparison.`<`, List(-1), presets = ops)
  }

  test("should compare top two stack elements with greater-than") {
    assertDataStack(Comparison.`>`, List(0), presets = ops)
  }

  test("should compare top stack element with less-than-zero") {
    assertDataStack(`0<`, List(0, 3), presets = ops)
  }

  test("should compare top stack element with equal-to-zero") {
    assertDataStack(`0=`, List(0, 3), presets = ops)
  }

  test("should compare top stack element with not-equal-to-zero") {
    assertDataStack(`0<>`, List(-1, 3), presets = ops)
  }

  test("should compare top stack element with greater-than-zero") {
    assertDataStack(`0>`, List(-1, 3), presets = ops)
  }

  test("should compare top stack element is not within bounds") {
    val withinBounds = sequence(
      dataStack(push(3)),
      dataStack(push(12)),
      dataStack(push(19)))

    assertDataStack(WITHIN, List(0), presets = withinBounds)
  }

  test("should compare top stack element is within bounds") {
    val withinBounds = sequence(
      dataStack(push(15)),
      dataStack(push(12)),
      dataStack(push(19)))

    assertDataStack(WITHIN, List(-1), presets = withinBounds)
  }
}
