package byok3.primitives

import byok3._
import byok3.data_structures.Context._
import byok3.data_structures.Stack._
import byok3.data_structures.{Address, Data}

class MemoryTest extends PrimitivesTestBase {

  describe("Memory") {
    it("should poke a value in memory") {
      val ops = sequence(
        dataStack(push(0x1000)),
        dataStack(push(0x77)),
        Memory.`!`)

      val ctx = ops.run(emptyContext).value._1
      assert(ctx.mem.peek(Address(0x1000)) === Data(0x77))
    }

    it("should write a value in memory and read back from memory") {
      val ops = sequence(
        dataStack(push(0x1000)),
        dataStack(push(0x77)),
        Memory.`!`,
        dataStack(push(0x1000)),
        Memory.`@`)

      val ctx = ops.run(emptyContext).value._1
      assert(ctx.ds === List(0x77))
    }

    it("should read back from empty memory") {
      val ops = sequence(
        dataStack(push(0x1000)),
        Memory.`@`)

      val ctx = ops.run(emptyContext).value._1
      assert(ctx.ds === List(0))
    }

    it("should error when invalid (negative) memory location accessed") {
      val ops = sequence(
        dataStack(push(-2)),
        Memory.`@`)

      val ex = intercept[StackMachineException] {
        ops.run(emptyContext).value
      }
      assert(ex.getMessage === "invalid memory address")
    }

    it("should error when invalid memory location accessed") {
      val ops = sequence(
        dataStack(push(emptyContext.mem.size + 5)),
        Memory.`@`)

      val ex = intercept[StackMachineException] {
        ops.run(emptyContext).value
      }
      assert(ex.getMessage === "invalid memory address")
    }

    it("should increment a value in memory") {
      val ops = sequence(
        dataStack(push(0x1000)),
        dataStack(push(0x77)),
        Memory.`!`,
        dataStack(push(0x1000)),
        Memory.`+!`)

      val ctx = ops.run(emptyContext).value._1
      assert(ctx.mem.peek(Address(0x1000)) === Data(0x78))
    }
  }
}
