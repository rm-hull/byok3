package byok3

import byok3.data_structures.Stack
import byok3.data_structures.Stack._
import org.scalatest.FunSpec


class StackTest extends FunSpec {

  describe("Push") {
    it("should append to an empty stack") {
      val ops = sequence(push(10), push(3), push(6))
      assert(ops.run(Stack.empty).value._1 === List(6, 3, 10))
    }

    it("should append to an existing stack") {
      val stack = List(6, 3, 10)
      val ops = sequence(push(19), push(12))
      assert(ops.run(stack).value._1 === List(12, 19, 6, 3, 10))
    }
  }

  describe("Pop") {
    it("should pop in the reverse order") {
      val ops = for {
        _ <- push(4)
        _ <- push(7)
        _ <- push(9)
        a <- pop
        b <- pop
        c <- pop
      } yield (a, b, c)

      assert(ops.run(Stack.empty).value === (List.empty, (9, 7, 4)))
    }

    it("should throw an exception on stack underflow") {
      val ops = for {
        _ <- push(4)
        _ <- push(7)
        a <- pop
        b <- pop
        c <- pop
      } yield (a, b, c)

      val ex = intercept[StackMachineException] {
        ops.run(Stack.empty).value
      }
      assert(ex.getMessage === "stack underflow")
    }
  }
}