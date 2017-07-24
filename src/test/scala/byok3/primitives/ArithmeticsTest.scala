package byok3.primitives

import byok3.data_structures.Context.dataStack
import byok3.data_structures.Stack.push
import byok3.sequence

class ArithmeticsTest extends PrimitivesTestBase {

  override val ops = sequence(dataStack(push(4)), dataStack(push(8)), dataStack(push(2)))

  describe("Arithmetics") {
    it("should add top two stack elements") {
      assertDataStack(Arithmetics.+, List(10, 4))
    }
    it("should subtract top two stack elements") {
      assertDataStack(Arithmetics.-, List(6, 4))
    }
    it("should multiply top two stack elements") {
      assertDataStack(Arithmetics.*, List(16, 4))
    }
    it("should divide top two stack elements") {
      assertDataStack(Arithmetics./, List(4, 4))
    }
    it("should multiply and divide top three stack elements") {
      assertDataStack(Arithmetics.`*/`, List(16))
    }
    it("should multiply, divide and mod top three stack elements") {
      assertDataStack(Arithmetics.`*/MOD`, List(16, 16))
    }
  }
}