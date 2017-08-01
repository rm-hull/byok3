package byok3.primitives

import byok3.data_structures.Context._
import byok3.data_structures.Error
import byok3.data_structures.Stack._
import byok3.helpers.sequence
import cats.implicits._

class MemoryTest extends PrimitivesTestBase {

  describe("Memory") {
    it("should poke a value in memory") {
      val ops = sequence(
        dataStack(push(0x77)),
        dataStack(push(0x1000)),
        Memory.`!`)

      val ctx = ops.runS(emptyContext).get
      assert(ctx.mem.peek(0x1000) == 0x77)
    }

    it("should write a value in memory and read back from memory") {
      val ops = sequence(
        dataStack(push(0x77)),
        dataStack(push(0x1000)),
        Memory.`!`,
        dataStack(push(0x1000)),
        Memory.`@`)

      val ctx = ops.runS(emptyContext).get
      assert(ctx.ds == List(0x77))
    }

    it("should read back from empty memory") {
      val ops = sequence(
        dataStack(push(0x1000)),
        Memory.`@`)

      val ctx = ops.runS(emptyContext).get
      assert(ctx.ds == List(0))
    }

    it("should error when invalid (negative) memory location accessed") {
      val ops = sequence(
        dataStack(push(-2)),
        Memory.`@`)

      val ex = intercept[IndexOutOfBoundsException] {
        ops.runS(emptyContext).get
      }
      assert(ex.getMessage == "-2")
    }

    it("should error when invalid memory location accessed") {
      val ops = sequence(
        dataStack(push(emptyContext.mem.size + 5)),
        Memory.`@`)

      val ex = intercept[IndexOutOfBoundsException] {
        ops.runS(emptyContext).get
      }
      assert(ex.getMessage == s"${emptyContext.mem.size + 5}")
    }

    it("should increment a value in memory") {
      val ops = sequence(
        dataStack(push(0x77)),
        dataStack(push(0x1000)),
        Memory.`!`,
        dataStack(push(0x1000)),
        Memory.`+!`)

      val ctx = ops.runS(emptyContext).get
      assert(ctx.mem.peek(0x1000) == 0x78)
    }

    it("should parse a string to the next delimiter") {
      val ops = sequence(
        input("IGNORED: HELLO WORLD").map(_ => ()),
        dataStack(push('R'.toInt)),
        Memory.PARSE)
      val ctx = ops.runS(emptyContext).get
      assert(ctx.ds == List(9, 8))
    }

    it("should return zero length if unable parse to the next delimiter") {
      val ops = sequence(
        input("IGNORED: HELLO WORLD").map(_ => ()),
        dataStack(push('!'.toInt)),
        Memory.PARSE)
      val ctx = ops.runS(emptyContext).get
      assert(ctx.ds == List(9, 0))
    }

    it("should push DP to the stack") {
      val ctx = Memory.DP.runS(emptyContext).get
      assert(ctx.ds == List(ctx.reg.dp))
    }

    it("should push TIB to the stack") {
      val ctx = Memory.TIB.runS(emptyContext).get
      assert(ctx.ds == List(ctx.reg.tib))
    }

    it("should store TOS in memory at the DP") {
      val ops = sequence(dataStack(push(19)), Memory.`,`)
      val ctx = ops.runS(emptyContext).get
      assert(ctx.ds == List.empty)
      assert(ctx.reg.dp == emptyContext.reg.dp + 1)
      assert(ctx.mem.peek(emptyContext.reg.dp) == 19)
    }

    it("should post-increment IP and push to the stack") {
      val ctx = Memory.`(LIT)`.runS(emptyContext).get
      assert(ctx.ds == List(emptyContext.reg.ip))
      assert(ctx.reg.ip == emptyContext.reg.ip + 1)
    }

    it("should throw an error") {
      val ops = sequence(dataStack(push(3)), dataStack(push(-3)), returnStack(push(9)), Memory.THROW)
      val ex = ops.runS(emptyContext).failed.get
      assert(ex == Error(-3))
    }

    it("should conditionally throw an error when NOS non-zero") {
      val ops = sequence(dataStack(push(3)), dataStack(push(-6)), Memory.`?ERROR`)
      val ex = ops.runS(emptyContext).failed.get
      assert(ex == Error(-6))
    }

    it("should not throw an error when NOS zero") {
      val ops = sequence(dataStack(push(0)), dataStack(push(-6)), Memory.`?ERROR`, dataStack(push(19)))
      val ctx = ops.runS(emptyContext).get
      assert(ctx.ds == List(19))
    }
  }
}
