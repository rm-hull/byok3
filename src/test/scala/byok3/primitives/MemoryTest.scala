package byok3.primitives

import byok3.data_structures.Context._
import byok3.data_structures.Stack._
import byok3.helpers.sequence
import cats.implicits._

class MemoryTest extends PrimitivesTestBase {

  test("should poke a value in memory") {
    val ops = sequence(
      dataStack(push(0x77)),
      dataStack(push(0x1000)),
      Memory.`!`)

    val ctx = ops.runS(emptyContext).get
    ctx.mem.peek(0x1000) shouldEqual 0x77
  }

  test("should write a value in memory and read back from memory") {
    val ops = sequence(
      dataStack(push(0x77)),
      dataStack(push(0x1000)),
      Memory.`!`,
      dataStack(push(0x1000)),
      Memory.`@`)

    val ctx = ops.runS(emptyContext).get
    ctx.ds shouldEqual List(0x77)
  }

  test("should read back from empty memory") {
    val ops = sequence(
      dataStack(push(0x1000)),
      Memory.`@`)

    val ctx = ops.runS(emptyContext).get
    ctx.ds shouldEqual List(0)
  }

  test("should error when invalid (negative) memory location accessed") {
    val ops = sequence(
      dataStack(push(-2)),
      Memory.`@`)

    val ex = intercept[IndexOutOfBoundsException] {
      ops.runS(emptyContext).get
    }
    ex.getMessage shouldEqual "0xFFFFFFFE"
  }

  test("should error when invalid memory location accessed") {
    val ops = sequence(
      dataStack(push(emptyContext.mem.size + 5)),
      Memory.`@`)

    val ex = intercept[IndexOutOfBoundsException] {
      ops.runS(emptyContext).get
    }
    ex.getMessage shouldEqual f"0x${emptyContext.mem.size + 5}%08X"
  }

  test("should increment a value in memory") {
    val ops = sequence(
      dataStack(push(0x77)),
      dataStack(push(0x1000)),
      Memory.`!`,
      dataStack(push(0x1000)),
      Memory.`+!`)

    val ctx = ops.runS(emptyContext).get
    ctx.mem.peek(0x1000) shouldEqual 0x78
  }

  test("should parse a string to the next delimiter") {
    val offset = 32 + 9
    val ops = sequence(
      input("IGNORED: HELLO WORLD").map(_ => ()),
      dataStack(push('R'.toInt)),
      Memory.PARSE)
    val ctx = ops.runS(emptyContext).get
    ctx.ds shouldEqual List(8, offset)
  }

  test("should return zero length if unable parse to the next delimiter") {
    val offset = 32 + 9
    val ops = sequence(
      input("IGNORED: HELLO WORLD").map(_ => ()),
      dataStack(push('!'.toInt)),
      Memory.PARSE)
    val ctx = ops.runS(emptyContext).get
    ctx.ds shouldEqual List(0, offset)
  }

  test("should push DP to the stack") {
    val ctx = Memory.DP.runS(emptyContext).get
    ctx.ds shouldEqual List(ctx.reg.dp)
  }

  test("should store TOS in memory at the DP") {
    val ops = sequence(dataStack(push(19)), Memory.`,`)
    val ctx = ops.runS(emptyContext).get
    ctx.ds shouldEqual List.empty
    ctx.reg.dp shouldEqual emptyContext.reg.dp + 1
    ctx.mem.peek(emptyContext.reg.dp) shouldEqual 19
  }
}
