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

import byok3.data_structures.Stack._
import byok3.helpers._
import cats.implicits._
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers

import scala.util.Success


class StackTest extends AnyFunSuite with Matchers {

  test("should push to an empty stack") {
    val ops = sequence(push(10), push(3), push(6))
    ops.runS(List.empty) shouldEqual Success(List(6, 3, 10))
  }

  test("should push to an existing stack") {
    val stack = List(6, 3, 10)
    val ops = sequence(push(19), push(12))
    ops.runS(stack) shouldEqual Success(List(12, 19, 6, 3, 10))
  }

  test("should peek the top element on the stack") {
    val ops = for {
      _ <- push(4)
      _ <- push(7)
      _ <- push(9)
      a <- peek
    } yield a
    ops.runA(List.empty) shouldEqual Success(9)
  }

  test("should fail when peeking an empty stack") {
    intercept[NoSuchElementException] {
      peek.run(List.empty).get
    }
  }

  test("should pop in the reverse order") {
    val ops = for {
      _ <- push(4)
      _ <- push(7)
      _ <- push(9)
      a <- pop
      b <- pop
      c <- pop
    } yield (a, b, c)

    ops.run(List.empty) shouldEqual Success(List.empty, (9, 7, 4))
  }

  test("should fail when popping an empty stack") {
    val ops = for {
      _ <- push(4)
      _ <- push(7)
      a <- pop
      b <- pop
      c <- pop
    } yield (a, b, c)

    intercept[NoSuchElementException] {
      ops.run(List.empty).get
    }
  }
}