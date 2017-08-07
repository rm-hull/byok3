package byok3.primitives

import byok3.data_structures.Context
import byok3.data_structures.Context.{dataStack, exec}
import byok3.data_structures.Stack.push
import byok3.helpers.sequence
import byok3.types.AppState
import cats.data.StateT
import cats.implicits._
import org.scalatest.{FunSuite, Matchers}

import scala.util.Try

abstract class PrimitivesTestBase extends FunSuite with Matchers {

  protected val emptyContext = Context(0x10000)
  protected val DP = exec("DP").runS(emptyContext).get.ds.head
  protected val IP = exec("IP").runS(emptyContext).get.ds.head

  def assertDataStack(op: AppState[Unit], expected: List[Int],
                      presets: StateT[Try, Context, List[Unit]] = sequence(dataStack(push(4)), dataStack(push(2)), dataStack(push(8)))) = {

    val effects = for {
      _ <- presets
      _ <- op
    } yield ()

    val result = effects.runS(emptyContext).get
    assert(result.ds == expected)
  }
}
