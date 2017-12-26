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

import byok3.data_structures.Error
import byok3.helpers.capturingOutput
import cats.implicits._
import org.scalatest.{FunSuite, Matchers}

import scala.util.Success

class InterpreterTest extends FunSuite with Matchers {

  test("should push values onto the data stack") {
    val ctx = Interpreter("9  5 3").runS(emptyContext)
    ctx.map(_.ds) shouldEqual Success(List(3, 5, 9))
  }

  test("should execute primitives sequentially") {
    val ctx = Interpreter(" 3 DUP * 2 -").runS(emptyContext)
    ctx.map(_.ds) shouldEqual Success(List(7))
  }

  test("should perform subtraction properly") {
    val ctx = Interpreter("5 2 -").runS(emptyContext)
    ctx.map(_.ds) shouldEqual Success(List(3))
  }

  test("should perform division properly") {
    val ctx = Interpreter("10 2 /").runS(emptyContext)
    ctx.map(_.ds) shouldEqual Success(List(5))
  }

  test("should record an error when stack underflow occurs") {
    val ctx = Interpreter("10 +").runS(emptyContext)
    assert(ctx.map(_.status) == Success(Left(Error(-4))))
    ctx.map(_.ds) shouldEqual Success(List.empty)
    ctx.map(_.rs) shouldEqual Success(List.empty)
  }

  test("should record an error when accessing invalid memory") {
    val ctx = Interpreter("-2 @").runS(emptyContext)
    assert(ctx.map(_.status) == Success(Left(Error(-9, "0xFFFFFFFE"))))
    ctx.map(_.ds) shouldEqual Success(List.empty)
    ctx.map(_.rs) shouldEqual Success(List.empty)
  }

  test("should create a constant") {
    val ops = for {
      _ <- Interpreter("220 CONSTANT LIMIT")
      _ <- Interpreter("20 LIMIT +")
    } yield ()

    val ctx = ops.runS(emptyContext)
    ctx.map(_.ds) shouldEqual Success(List(240))
  }

  test("should create a variable") {
    val ops = for {
      _ <- Interpreter("VARIABLE DATE")
      _ <- Interpreter("12 DATE !")
      _ <- Interpreter("DATE @ 3 +")
    } yield ()

    val ctx = ops.runS(emptyContext)
    ctx.map(_.ds) shouldEqual Success(List(15))
  }

  test("should parse the input stream") {
    val ops = Interpreter("33 PARSE BEYOND SPACE! 42")
    val ctx = ops.runS(emptyContext)
    ctx.map(_.ds) shouldEqual Success(List(42, 12, 9))

    Stream.from(9).zip("BEYOND SPACE").foreach {
      case (addr, ch) => ctx.map(_.mem.char_peek(addr)) shouldEqual Success(ch)
    }
  }

  test("should record an error on unfound word") {
    val ctx = Interpreter("10 4 + SAUSAGES 19 4 -").runS(emptyContext)
    ctx.map(_.status) shouldEqual Success(Left(Error(-13, "SAUSAGES")))
    ctx.map(_.ds) shouldEqual Success(List.empty)
    ctx.map(_.rs) shouldEqual Success(List.empty)
  }

  test("should record an error when user-defined throw occurs") {
    val ctx = Interpreter("10 4 + THROW 19 4 -").runS(emptyContext)
    ctx.map(_.status) shouldEqual Success(Left(Error(14)))
    ctx.map(_.ds) shouldEqual Success(List.empty)
    ctx.map(_.rs) shouldEqual Success(List.empty)
  }

  test("should print the top stack item") {
    val result = capturingOutput {
      val ctx = Interpreter("1 2 3 5 7 + . . .").runS(emptyContext)
      ctx.map(_.ds) shouldEqual Success(List(1))
    }
    result shouldEqual "12 3 2 "
  }

  test("should print the stack") {
    val result = capturingOutput {
      val ctx = Interpreter("1 2 3 5 7 + .S").runS(emptyContext)
      ctx.map(_.ds) shouldEqual Success(List(12, 3, 2, 1))
    }
    result shouldEqual "1 2 3 12 "
  }

  test("should parse and print the message") {
    val result = capturingOutput {
      Interpreter("33 PARSE HELLO, WORLD! TYPE 10 EMIT 33 PARSE THAT IS ALL! TYPE").runS(emptyContext)
    }
    result shouldEqual "HELLO, WORLD\nTHAT IS ALL"
  }

  test("should compile and run a user defined word") {
    val ops = for {
      _ <- Interpreter(": SQR DUP * ;")
      _ <- Interpreter("3 SQR")
    } yield ()

    val ctx = ops.runS(emptyContext)
    ctx.map(_.ds) shouldEqual Success(List(9))
  }

  test("should compile and run a user defined word that compiles looping primitives") {
    val ops = for {
      _ <- Interpreter(": DECADE 10 0 DO I LOOP ;")
      _ <- Interpreter("DECADE")
    } yield ()

    val ctx = ops.runS(emptyContext)
    ctx.map(_.ds) shouldEqual Success(List(9, 8, 7, 6, 5, 4, 3, 2, 1, 0))
  }

  test("should compile and run a user defined word that compiles branching & string primitives") {
    val ops = for {
      _ <- Interpreter(": ?DOZEN  12 = IF .\" dozen \" ELSE .\" not a dozen\" THEN ;")
      _ <- Interpreter("8 ?DOZEN")
    } yield ()

    val actual = capturingOutput {
      ops.runS(emptyContext)
    }
    actual shouldEqual "not a dozen"
  }

  test("should compile and run a user defined word over several sessions") {
    val ops = for {
      _ <- Interpreter(": GCD ( a b -- n )")
      _ <- Interpreter("    begin  dup while  tuck mod  repeat drop ;")
      _ <- Interpreter("60 201 GCD")
    } yield ()

    val ctx = ops.runS(emptyContext)
    ctx.map(_.ds) shouldEqual Success(List(3))
  }

  test("should record error when LITERAL when not in compile mode") {
    val ctx = Interpreter("10 LITERAL").runS(emptyContext)
    ctx.map(_.status) shouldEqual Success(Left(Error(-14)))
  }
}
