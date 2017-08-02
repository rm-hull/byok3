package byok3.data_structures

import byok3.data_structures.Tokenizer._
import org.scalatest.{FunSuite, Matchers}

class TokenizerTest extends FunSuite with Matchers {

  test("should parse a single token") {
    val in = "10 20 +"
    assert(Tokenizer(in) === Token("10", 0, in))
  }

  test("should parse the next token") {
    val in = "10 20 +"
    assert(Tokenizer(in).next(delimiters) === Token("20", 3, in))
  }

  test("should parse the entire input if no match") {
    val in = "HELLO"
    assert(Tokenizer(in) === Token("HELLO", 0, in))
  }

  test("should return no token if at end") {
    val in = "10 20 +"
    assert(Tokenizer(in).next(delimiters).next(delimiters).next(delimiters) === EndOfData)
  }

  test("should parse with different delimiters") {
    val in = "3 ( this is a comment ) 4"
    assert(Tokenizer(in).next("\\)") === Token("( this is a comment ", 2, in))
  }

  test("should not squash multiple delimiters") {
    val in = "10  20 +"
    assert(Tokenizer(in).next(delimiters) === Token("", 3, in))
  }

  test("should return no token if delimiter not found") {
    val in = "HELLO, WORLD!"
    assert(Tokenizer(in).next(":").exhausted)
  }
}
