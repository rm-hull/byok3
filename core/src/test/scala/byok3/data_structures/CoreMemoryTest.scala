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

import scala.util.{Failure, Try}

class CoreMemoryTest extends AnyFunSuite with Matchers {

  private val mem = CoreMemory(0x2000)

  test("should poke and peek on aligned memory") {
    val result = mem.poke(0x24, 0x12345678)
    result.peek(0x24) shouldEqual 0x12345678
  }

  test("should throw error -9 on poking invalid memory") {
    Try(mem.poke(0x2002, 0x12345678)) shouldEqual Failure(Error(-9, "0x00002002"))
  }

  test("should throw error -9 on peeking invalid memory") {
    Try(mem.peek(-4)) shouldEqual Failure(Error(-9, "0xFFFFFFFC"))
  }

  test("should throw error -23 on poking unaligned address") {
    Try(mem.poke(0x0123, 0x12345678)) shouldEqual Failure(Error(-23, "0x00000123"))
  }

  test("should throw error -23 on peeking unaligned address") {
    Try(mem.peek(0x0987)) shouldEqual Failure(Error(-23, "0x00000987"))
  }

  test("should throw error -9 on char-peeking invalid memory") {
    Try(mem.char_peek(0x5123)) shouldEqual Failure(Error(-9, "0x00005123"))
  }

  test("should char-peek") {
    val result = mem.poke(0x24, 0x12345678)
    result.char_peek(0x24) shouldEqual 0x78
    result.char_peek(0x25) shouldEqual 0x56
    result.char_peek(0x26) shouldEqual 0x34
    result.char_peek(0x27) shouldEqual 0x12
  }

  test("should char-poke slot 1") {
    val result = mem.poke(0x24, 0x12345678).char_poke(0x24, 0xAB)
    result.peek(0x24) shouldEqual 0x123456AB
    result.char_peek(0x24) shouldEqual 0xAB
  }

  test("should char-poke slot 2") {
    val result = mem.poke(0x24, 0x12345678).char_poke(0x25, 0xAB)
    result.peek(0x24) shouldEqual 0x1234AB78
    result.char_peek(0x25) shouldEqual 0xAB
  }

  test("should char-poke slot 3") {
    val result = mem.poke(0x24, 0x12345678).char_poke(0x26, 0xAB)
    result.peek(0x24) shouldEqual 0x12AB5678
    result.char_peek(0x26) shouldEqual 0xAB
  }

  test("should char-poke slot 4") {
    val result = mem.poke(0x24, 0x12345678).char_poke(0x27, 0xAB)
    result.peek(0x24) shouldEqual 0xAB345678
    result.char_peek(0x27) shouldEqual 0xAB
  }


}
