package byok3

import byok3.data_structures.Context
import org.scalatest.FunSpec

class InterpreterTest extends FunSpec {

  val emptyContext = Context(0x10000)

  describe("tokenize") {
    it("should stream tokens separated by whitespace") {
      assert(Interpreter.tokenize("10\t\t 3\t6 \nHELLO\n\n").toList ===
        List("10", "3", "6", "HELLO"))
    }
  }

  describe("Interpreter") {
    it("should push values onto the data stack") {
      val ops = Interpreter.exec("9 5 3")
      val result = ops.run(emptyContext).value
      assert(result._1.ds === List(3, 5, 9))
    }

    it("should execute primitives sequentially") {
      val ops = Interpreter.exec("3 DUP * 2 -")
      val result = ops.run(emptyContext).value
      assert(result._1.ds === List(7))
    }

    it("should perform subtraction properly") {
      val ops = Interpreter.exec("5 2 -")
      val result = ops.run(emptyContext).value
      assert(result._1.ds === List(3))
    }

    it("should perform division properly") {
      val ops = Interpreter.exec("10 2 /")
      val result = ops.run(emptyContext).value
      assert(result._1.ds === List(5))
    }


  }
}
