package byok3.primitives

import byok3.data_structures.Context
import byok3.data_structures.Context.dataStack
import byok3.data_structures.Stack.push
import byok3.helpers.sequence
import cats.data.State
import org.scalatest.FunSpec

abstract class PrimitivesTestBase extends FunSpec {

  protected val emptyContext = Context(0x10000)
  protected val ops = sequence(dataStack(push(4)), dataStack(push(2)), dataStack(push(8)))

  def assertDataStack(op: State[Context, Unit], expected: List[Int]) = {
    val effects = for {
      _ <- ops
      _ <- op
    } yield ()

    val result = effects.run(emptyContext).value
    assert(result._1.ds === expected)
  }
}
