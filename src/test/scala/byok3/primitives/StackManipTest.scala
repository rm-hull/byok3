package byok3.primitives

import byok3.data_structures.Context._
import byok3.data_structures.Stack.push
import byok3.helpers.sequence
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

    it("should move x from data stack to return stack") {
      val ops = sequence(dataStack(push(4)), dataStack(push(2)), dataStack(push(8)), `>R`)
      val ctx = ops.run(emptyContext).value._1
      assert(ctx.rs === List(8))
      assert(ctx.ds === List(2, 4))
    }

    it("should move x from return stack to data stack") {
      val ops = sequence(returnStack(push(4)), dataStack(push(2)), dataStack(push(8)), `R>`)
      val ctx = ops.run(emptyContext).value._1
      assert(ctx.rs === List())
      assert(ctx.ds === List(4, 8, 2))
    }
  }
}