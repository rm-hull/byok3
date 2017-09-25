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
