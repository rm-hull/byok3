package byok3.primitives

import byok3.data_structures.Context.dataStack
import byok3.data_structures.Stack.push
import byok3.helpers.sequence
import byok3.primitives.BitLogic._
import cats.implicits._

class BitLogicTest extends PrimitivesTestBase {

  val ops = sequence(dataStack(push(3)), dataStack(push(19)))

  test("should bitwise-and top two stack elements") {
    assertDataStack(AND, List(3 & 19), presets = ops)
  }

  test("should bitwise-or top two stack elements") {
    assertDataStack(OR, List(3 | 19), presets = ops)
  }

  test("should bitwise-xor top two stack elements") {
    assertDataStack(XOR, List(3 ^ 19), presets = ops)
  }

  test("should bitwise-invert top stack element") {
    assertDataStack(INVERT, List(~19, 3), presets = ops)
  }

  test("should bitwise-shift-left top stack element by NOS bits") {
    assertDataStack(LSHIFT, List(3 << 19), presets = ops)
  }

  test("should bitwise-shift-right top stack element by NOS bits") {
    assertDataStack(RSHIFT, List(3 >> 19), presets = ops)
  }
}
