package byok3.primitives

import byok3.data_structures.Context._
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
      assert(ex.getMessage == "invalid memory address: -2")
    }

    it("should error when invalid memory location accessed") {
      val ops = sequence(
        dataStack(push(emptyContext.mem.size + 5)),
        Memory.`@`)

      val ex = intercept[IndexOutOfBoundsException] {
        ops.runS(emptyContext).get
      }
      assert(ex.getMessage == s"invalid memory address: ${emptyContext.mem.size + 5}")
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
  }
}
