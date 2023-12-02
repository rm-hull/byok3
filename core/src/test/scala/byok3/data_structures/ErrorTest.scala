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

package byok3.data_structures



import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers

import scala.util.Try

class ErrorTest extends AnyFunSuite with Matchers {

  test("should transform divide by zero to error -10") {
    val ex = Try(1 / 0).failed.get
    Error(ex) shouldEqual Error(-10)
  }

  test("should transform index out of bounds to error -9") {
    val ex = Try(List.empty(17)).failed.get
    Error(ex) shouldEqual Error(-9, "17")
  }

  test("should transform no such element to error -4") {
    val ex = Try(List.empty.head).failed.get
    Error(ex) shouldEqual Error(-4)
  }

  test("should not transform existing error") {
    Error(Error(342)) shouldEqual Error(342)
  }

  test("should transform uncaught throwable to error 0") {
    Error(new RuntimeException("unit test err")) shouldEqual Error(0, "[java.lang.RuntimeException] unit test err")
  }
}
