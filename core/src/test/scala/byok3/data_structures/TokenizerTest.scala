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

import byok3.data_structures.Tokenizer._
import org.scalatest.{FunSuite, Matchers}

class TokenizerTest extends FunSuite with Matchers {

  test("should parse a single token") {
    val in = "10 20 +"
    Tokenizer(in) shouldEqual Token("10", 0, in)
  }

  test("should parse the next token") {
    val in = "10 20 +"
    Tokenizer(in).next(delimiters) shouldEqual Token("20", 3, in)
  }

  test("should parse the entire input if no match") {
    val in = "HELLO"
    Tokenizer(in) shouldEqual Token("HELLO", 0, in)
  }

  test("should return no token if at end") {
    val in = "10 20 +"
    Tokenizer(in).next(delimiters).next(delimiters).next(delimiters) shouldEqual EndOfData
  }

  test("should parse with different delimiters") {
    val in = "3 ( this is a comment ) 4"
    Tokenizer(in).next("\\)") shouldEqual Token("( this is a comment ", 2, in)
  }
  
  test("should greedily consume multiple delimiters") {
    val in = "10  20 +"
    Tokenizer(in).next(delimiters) shouldEqual Token("20", 4, in)
  }

  test("should return no token if delimiter not found") {
    val in = "HELLO, WORLD!"
    Tokenizer(in).next(":").exhausted shouldBe true
  }
  
  test("should handle null properly (jline's linereader may inject null)") {
    Tokenizer(null) shouldEqual EndOfData
  }
}
