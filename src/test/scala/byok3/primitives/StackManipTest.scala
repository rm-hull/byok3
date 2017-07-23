package byok3.primitives

import byok3.primitives.StackManip._

class StackManipTest extends PrimitivesTestBase {

  describe("StackManip") {
    it("should count the stack depth") {
      assertDataStack(depth, List(3, 8, 2, 4))
    }

    it("should drop top stack element") {
      assertDataStack(drop, List(2, 4))
    }

    it("should swap the top two elements on the stack") {
      assertDataStack(swap, List(2, 8, 4))
    }

    it("should duplicate the top element on the stack") {
      assertDataStack(dup, List(8, 8, 2, 4))
    }
  }
}