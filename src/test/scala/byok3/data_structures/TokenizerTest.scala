package byok3.data_structures

import org.scalatest.FunSpec

class TokenizerTest extends FunSpec {

  val delimiters = "[ \t\n]"

  describe("Tokenizer") {
    it("should parse a single token") {
      val in = "10 20 +"
      assert(Tokenizer(in) === Token("10", in, 3))
    }

    it("should parse the next token") {
      val in = "10 20 +"
      assert(Tokenizer(in).next(delimiters) === Token("20", in, 6))
    }

    it("should parse the entire input if no match") {
      val in = "HELLO"
      assert(Tokenizer(in) === Token("HELLO", in, 6))
    }

    it("should return no token if at end") {
      val in = "10 20 +"
      assert(Tokenizer(in).next(delimiters).next(delimiters).next(delimiters).next(delimiters) === EndOfData)
    }

    it("should parse with different delimiters") {
      val in = "3 ( this is a comment ) 4"
      assert(Tokenizer(in).next("\\)") === Token("( this is a comment ", in, 23))
    }

    it("should not squash multiple delimiters") {
      val in = "10  20 +"
      assert(Tokenizer(in).next(delimiters) === Token("", in, 4))
    }
  }
}
