package byok3.primitives

import byok3.primitives.Memory._
import byok3.data_structures.Context._
import byok3.data_structures.Error
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
      `!`,
      dataStack(push(0x1000)),
      `@`)

    val ctx = ops.runS(emptyContext).get
    ctx.ds shouldEqual List(0x77)
  }

  test("should read back from empty memory") {
    val ops = sequence(dataStack(push(0x1000)), `@`)
    val ctx = ops.runS(emptyContext).get
    ctx.ds shouldEqual List(0)
  }

  test("should error when invalid (negative) memory location accessed") {
    val ops = sequence(dataStack(push(-20)), `@`)
    ops.runS(emptyContext).failed.get shouldEqual Error(-9, "0xFFFFFFEC")
  }

  test("should error when invalid memory location accessed") {
    val ops = sequence(dataStack(push(0x00060000)), `@`)
    ops.runS(emptyContext).failed.get shouldEqual Error(-9, "0x00060000")
  }

  test("should accumulate a value in memory") {
    val ops = sequence(
      dataStack(push(0x2)),
      dataStack(push(0x77)),
      dataStack(push(0x1000)),
      `!`,
      dataStack(push(0x1000)),
      `+!`)

    val ctx = ops.runS(emptyContext).get
    ctx.mem.peek(0x1000) shouldEqual 0x79
  }

  test("should parse a string to the next delimiter") {
    val offset = 9
    val ops = sequence(
      input("IGNORED: HELLO WORLD").map(_ => ()),
      dataStack(push('R'.toInt)),
      PARSE)
    val ctx = ops.runS(emptyContext).get
    ctx.ds shouldEqual List(8, offset)
  }

  test("should return zero length if unable parse to the next delimiter") {
    val offset = 9
    val ops = sequence(
      input("IGNORED: HELLO WORLD").map(_ => ()),
      dataStack(push('!'.toInt)),
      PARSE)
    val ctx = ops.runS(emptyContext).get
    ctx.ds shouldEqual List(0, offset)
  }

  test("should push HERE to the stack") {
    val ctx = HERE.runS(emptyContext).get
    ctx.ds shouldEqual List(ctx.reg.dp)
  }

  test("should store TOS in memory at the DP") {
    val ops = sequence(
      dataStack(push(19)), `,`,
      dataStack(push(34)), `,`)
    val ctx = ops.runS(emptyContext).get
    ctx.ds shouldEqual List.empty
    ctx.reg.dp shouldEqual emptyContext.reg.dp + 4 + 4
    ctx.mem.peek(emptyContext.reg.dp + 0) shouldEqual 19
    ctx.mem.peek(emptyContext.reg.dp + 4) shouldEqual 34
    ctx.mem.peek(emptyContext.reg.dp + 8) shouldEqual 0
  }

  test("should post-increment IP and push to the stack") {
    val ops = sequence(
      dataStack(push(27)),
      dataStack(push(emptyContext.reg.ip)),
      `!`,
      `(LIT)`)

    val ctx = ops.runS(emptyContext).get
    ctx.ds shouldEqual List(27)
    ctx.reg.ip shouldEqual emptyContext.reg.ip + 4
  }
}
