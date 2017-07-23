package byok3.primitives

import byok3.Stack._
import byok3.primitives.StackManip._
import byok3.{Sequence, Stack}
import org.scalatest.FunSpec

class StackManipTest extends FunSpec {

  describe("Drop") {
    it("should remove top stack element") {
      val ops = Sequence(push(3), push(7), push(9), drop)
      assert(ops.run(Stack.empty).value._1 === List(7, 3))
    }

    it("should throw error when stack empty") {
      val ex = intercept[RuntimeException] {
        drop.run(Stack.empty).value
      }
      assert(ex.getMessage === "Empty stack")
    }
  }

  describe("Swap") {
    it("should swap the top two elements on the stack") {
      val ops = Sequence(push(3), push(7), push(9), swap)
      assert(ops.run(Stack.empty).value._1 === List(7, 9, 3))
    }

    it("should throw error when stack empty") {
      val ops = Sequence(push(3), swap)
      val ex = intercept[RuntimeException] {
        ops.run(Stack.empty).value
      }
      assert(ex.getMessage === "Empty stack")
    }
  }

  describe("Dup") {
    it("should duplicate the top element on the stack") {
      val ops = Sequence(push(3), push(7), push(9), dup)
      assert(ops.run(Stack.empty).value._1 === List(9, 9, 7, 3))
    }

    it("should throw error when stack empty") {
      val ex = intercept[RuntimeException] {
        dup.run(Stack.empty).value
      }
      assert(ex.getMessage === "Empty stack")
    }
  }
}
