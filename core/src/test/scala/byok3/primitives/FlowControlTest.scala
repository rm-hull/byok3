package byok3.primitives

import byok3.data_structures.Context._
import byok3.data_structures.Error
import byok3.data_structures.Stack._
import byok3.primitives.FlowControl._
import byok3.helpers.sequence
import cats.implicits._

class FlowControlTest extends PrimitivesTestBase {

  test("should throw an error") {
    val ops = sequence(dataStack(push(3)), dataStack(push(-3)), returnStack(push(9)), FlowControl.THROW)
    val ex = ops.runS(emptyContext).failed.get
    ex shouldEqual Error(-3)
  }

  test("should conditionally throw an error when NOS non-zero") {
    val ops = sequence(dataStack(push(3)), dataStack(push(-6)), `?ERROR`)
    val ex = ops.runS(emptyContext).failed.get
    ex shouldEqual Error(-6)
  }

  test("should not throw an error when NOS zero") {
    val ops = sequence(dataStack(push(0)), dataStack(push(-6)), `?ERROR`, dataStack(push(19)))
    val ctx = ops.runS(emptyContext).get
    ctx.ds shouldEqual List(19)
  }
}
