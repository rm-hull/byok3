package byok3.primitives

class ArithmeticsTest extends PrimitivesTestBase {

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
      assertDataStack(Arithmetics.`*/`, List(4))
    }
    it("should multiply, divide and mod top three stack elements") {
      assertDataStack(Arithmetics.`*/MOD`, List(4, 4))
    }
  }
}