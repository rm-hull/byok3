package byok3

import byok3.data_structures.Context
import org.scalatest.FunSpec

class InterpreterTest extends FunSpec {

  describe("Interpreter") {
    it("should push values onto the data stack") {
      val ops = Interpreter.exec("9 5 3")
      val result = ops.run(Context(0x10000)).value
      assert(result._1.ds === List(3, 5, 9))
    }

    it("should execute primitives sequentially") {
      val ops = Interpreter.exec("3 DUP * 2 SWAP -")
      val result = ops.run(Context(0x10000)).value
      assert(result._1.ds === List(7))
    }
  }
}
