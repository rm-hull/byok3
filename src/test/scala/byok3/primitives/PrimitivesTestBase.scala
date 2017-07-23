package byok3.primitives

import byok3._
import byok3.data_structures.Context
import byok3.data_structures.Context.dataStack
import byok3.data_structures.Stack.push
import cats.data.State
import org.scalatest.FunSpec

abstract class PrimitivesTestBase extends FunSpec {

  val emptyContext = Context(0x10000)

  def assertDataStack(op: State[Context, Unit], expected: List[Int]) = {
    val ops = sequence(dataStack(push(4)), dataStack(push(2)), dataStack(push(8)), op)
    assert(ops.run(emptyContext).value._1.ds === expected)
  }
}
