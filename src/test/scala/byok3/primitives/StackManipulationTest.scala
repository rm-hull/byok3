package byok3.primitives

import byok3.data_structures.Context._
import byok3.data_structures.Stack.push
import byok3.helpers.sequence
import byok3.primitives.StackManipulation._
import cats.implicits._

class StackManipulationTest extends PrimitivesTestBase {

  describe("Stack Manipulation") {
    it("should count the stack depth") {
      assertDataStack(DEPTH, List(3, 8, 2, 4))
    }

    it("should drop top stack element") {
      assertDataStack(DROP, List(2, 4))
    }

    it("should swap the top two elements on the stack") {
      assertDataStack(SWAP, List(2, 8, 4))
    }

    it("should duplicate the top element on the stack") {
      assertDataStack(DUP, List(8, 8, 2, 4))
    }

    it("should duplicate the top element on the stack if non-zero") {
      assertDataStack(`?DUP`, List(8, 8, 2, 4))
    }

    it("should not duplicate the top element on the stack if zero") {
      val presets = sequence(dataStack(push(4)), dataStack(push(2)), dataStack(push(0)))
      assertDataStack(`?DUP`, List(0, 2, 4), presets)
    }

    it("should duplicate NOS to top of stack") {
      assertDataStack(OVER, List(2, 8, 2, 4))
    }

    it("should remove NOS") {
      assertDataStack(NIP, List(8, 4))
    }

    it("should tuck TOS") {
      assertDataStack(TUCK, List(8, 2, 8, 4))
    }

    it("should rotate top three stack items") {
      assertDataStack(ROT, List(4, 8, 2))
    }

    it("should rotate top three stack items in other direction") {
      assertDataStack(`-ROT`, List(2, 4, 8))
    }

    it("should count the return stack depth") {
      val ops = sequence(returnStack(push(4)), returnStack(push(2)), returnStack(push(8)), RDEPTH)
      val ctx = ops.runS(emptyContext).get
      assert(ctx.ds == List(3))
    }

    it("should move value from data stack to return stack") {
      val ops = sequence(dataStack(push(4)), dataStack(push(2)), dataStack(push(8)), `>R`)
      val ctx = ops.runS(emptyContext).get
      assert(ctx.rs == List(8))
      assert(ctx.ds == List(2, 4))
    }

    it("should move value from return stack to data stack") {
      val ops = sequence(returnStack(push(4)), dataStack(push(2)), dataStack(push(8)), `R>`)
      val ctx = ops.runS(emptyContext).get
      assert(ctx.rs == List())
      assert(ctx.ds == List(4, 8, 2))
    }

    it("should copy value from return stack to data stack") {
      val ops = sequence(returnStack(push(4)), dataStack(push(2)), dataStack(push(8)), `R@`)
      val ctx = ops.runS(emptyContext).get
      assert(ctx.rs == List(4))
      assert(ctx.ds == List(4, 8, 2))
    }
  }
}