package byok3.primitives

import byok3.data_structures.Context.dataStack
import byok3.data_structures.Stack.push
import byok3.helpers.sequence
import cats.implicits._

class ArithmeticsTest extends PrimitivesTestBase {

  val ops = sequence(dataStack(push(4)), dataStack(push(8)), dataStack(push(2)))

  describe("Arithmetics") {
    it("should add top two stack elements") {
      assertDataStack(Arithmetics.+, List(10, 4), presets = ops)
    }
    it("should subtract top two stack elements") {
      assertDataStack(Arithmetics.-, List(6, 4), presets = ops)
    }
    it("should multiply top two stack elements") {
      assertDataStack(Arithmetics.*, List(16, 4), presets = ops)
    }
    it("should divide top two stack elements") {
      assertDataStack(Arithmetics./, List(4, 4), presets = ops)
    }
    it("should multiply and divide top three stack elements") {
      assertDataStack(Arithmetics.`*/`, List(16), presets = ops)
    }
    it("should multiply, divide and mod top three stack elements") {
      assertDataStack(Arithmetics.`*/MOD`, List(16, 16), presets = ops)
    }
    it("should negate the top stack element") {
      assertDataStack(Arithmetics.NEGATE, List(-2, 8, 4), presets = ops)
    }
    it("should abs the top stack element") {
      assertDataStack(Arithmetics.ABS, List(2), presets = sequence(dataStack(push(-2))))
    }
    it("should pick max for top two stack elements") {
      assertDataStack(Arithmetics.MAX, List(8, 4), presets = ops)
    }
    it("should pick min for top two stack elements") {
      assertDataStack(Arithmetics.MIN, List(2, 4), presets = ops)
    }
  }
}