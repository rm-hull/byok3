package byok3.primitives

import byok3.data_structures.Context._
import byok3.data_structures.CoreMemory._
import byok3.data_structures.Error
import byok3.data_structures.MachineState.{OK, Smudge}
import byok3.data_structures.Stack._
import byok3.helpers._
import byok3.primitives.Compiler._
import byok3.primitives.Memory._
import cats.implicits._

import scala.util.Failure

class CompilerTest extends PrimitivesTestBase {

  test("literal should fail when not in compile mode") {
    val ops = sequence(machineState(OK), LITERAL)
    val ctx = ops.runS(emptyContext)
    ctx shouldEqual Failure(Error(-14))
  }

  test("literal should write (LIT) n when in compile mode") {
    val ops = sequence(machineState(Smudge), dataStack(push(32)), LITERAL, HERE)
    val ctx = ops.runS(emptyContext).get
    val here = ctx.ds.head
    ctx.dictionary.get(ctx.mem.peek(dec(dec(here)))).get.name shouldEqual "(LIT)"
    ctx.mem.peek(dec(here)) shouldEqual 32
  }
}
