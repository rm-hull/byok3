package byok3

import byok3.data_structures.Context
import cats.implicits._
import org.scalatest.FunSpec

class InterpreterTest extends FunSpec {

  val emptyContext = Context(0x10000)

  describe("tokenize") {
    it("should stream tokens separated by whitespace") {
      assert(Interpreter.tokenize("10\t\t 3\t6 \nHELLO\n\n").toList ==
        List("10", "3", "6", "HELLO"))
    }
  }

  describe("Interpreter") {
    it("should push values onto the data stack") {
      val result = Interpreter("9 5 3").runS(emptyContext).get
      assert(result.ds == List(3, 5, 9))
    }

    it("should execute primitives sequentially") {
      val result = Interpreter("3 DUP * 2 -").runS(emptyContext).get
      assert(result.ds == List(7))
    }

    it("should perform subtraction properly") {
      val result = Interpreter("5 2 -").runS(emptyContext).get
      assert(result.ds == List(3))
    }

    it("should perform division properly") {
      val result = Interpreter("10 2 /").runS(emptyContext).get
      assert(result.ds == List(5))
    }

    it("should record an error when stack underflow occurs") {
      intercept[NoSuchElementException] {
        Interpreter("10 +").runS(emptyContext).get
      }
    }

    it("should record an error when accessing invalid memory") {
      intercept[IndexOutOfBoundsException] {
        Interpreter("-2 @").runS(emptyContext).get
      }
    }
  }
}
