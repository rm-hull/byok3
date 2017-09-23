package byok3.primitives

import byok3.data_structures.Context.dataStack
import byok3.data_structures.Stack.push
import byok3.helpers.sequence
import byok3.primitives.Comparison._
import cats.implicits._

class ComparisonTest extends PrimitivesTestBase {

  val ops = sequence(dataStack(push(3)), dataStack(push(19)))

  test("should compare top two stack elements for equality") {
    assertDataStack(`=`, List(0), presets = ops)
  }

  test("should compare top two stack elements for not equality") {
    assertDataStack(`<>`, List(-1), presets = ops)
  }

  test("should compare top two stack elements with less-than") {
    assertDataStack(Comparison.`<`, List(-1), presets = ops)
  }

  test("should compare top two stack elements with greater-than") {
    assertDataStack(Comparison.`>`, List(0), presets = ops)
  }

  test("should compare top stack element with less-than-zero") {
    assertDataStack(`0<`, List(0, 3), presets = ops)
  }

  test("should compare top stack element with equal-to-zero") {
    assertDataStack(`0=`, List(0, 3), presets = ops)
  }

  test("should compare top stack element with not-equal-to-zero") {
    assertDataStack(`0<>`, List(-1, 3), presets = ops)
  }

  test("should compare top stack element with greater-than-zero") {
    assertDataStack(`0>`, List(-1, 3), presets = ops)
  }

  test("should compare top stack element is not within bounds") {
    val withinBounds = sequence(
      dataStack(push(3)),
      dataStack(push(12)),
      dataStack(push(19)))

    assertDataStack(WITHIN, List(0), presets = withinBounds)
  }

  test("should compare top stack element is within bounds") {
    val withinBounds = sequence(
      dataStack(push(15)),
      dataStack(push(12)),
      dataStack(push(19)))

    assertDataStack(WITHIN, List(-1), presets = withinBounds)
  }
}
