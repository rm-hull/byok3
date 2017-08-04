package byok3.primitives

import byok3.data_structures.Context._
import byok3.data_structures.Error
import byok3.data_structures.Stack.push
import byok3.helpers.sequence
import byok3.primitives.StackManipulation._
import byok3.types.AppState
import cats.implicits._

class StackManipulationTest extends PrimitivesTestBase {

  test("should count the stack depth") {
    assertDataStack(DEPTH, List(3, 8, 2, 4))
  }

  test("should drop top stack element") {
    assertDataStack(DROP, List(2, 4))
  }

  test("should drop top two stack elements") {
    assertDataStack(`2DROP`, List(4))
  }

  test("should swap the top two elements on the stack") {
    assertDataStack(SWAP, List(2, 8, 4))
  }

  test("should swap the top two pairs on the stack") {
    val presets =  sequence(dataStack(push(3)), dataStack(push(4)), dataStack(push(2)), dataStack(push(0)))
    assertDataStack(`2SWAP`, List(3, 4, 0, 2), presets)
  }

  test("should duplicate the top element on the stack") {
    assertDataStack(DUP, List(8, 8, 2, 4))
  }

  test("should duplicate the top pair on the stack") {
    assertDataStack(`2DUP`, List(8, 2, 8, 2, 4))
  }

  test("should duplicate the top element on the stack if non-zero") {
    assertDataStack(`?DUP`, List(8, 8, 2, 4))
  }

  test("should not duplicate the top element on the stack if zero") {
    val presets = sequence(dataStack(push(4)), dataStack(push(2)), dataStack(push(0)))
    assertDataStack(`?DUP`, List(0, 2, 4), presets)
  }

  test("should duplicate NOS to top of stack") {
    assertDataStack(OVER, List(2, 8, 2, 4))
  }

  test("should duplicate  to top of stack") {
    val presets =  sequence(dataStack(push(3)), dataStack(push(4)), dataStack(push(2)), dataStack(push(0)))
    assertDataStack(`2OVER`, List(4, 3, 0, 2, 4, 3), presets)
  }

  test("should remove NOS") {
    assertDataStack(NIP, List(8, 4))
  }

  test("should tuck TOS") {
    assertDataStack(TUCK, List(8, 2, 8, 4))
  }

  test("should rotate top three stack items") {
    assertDataStack(ROT, List(4, 8, 2))
  }

  test("should rotate top three stack items in other direction") {
    assertDataStack(`-ROT`, List(2, 4, 8))
  }

  test("should count the return stack depth") {
    val ops = sequence(returnStack(push(4)), returnStack(push(2)), returnStack(push(8)), RDEPTH)
    val ctx = ops.runS(emptyContext).get
    ctx.ds shouldEqual List(3)
  }

  test("should move value from data stack to return stack") {
    val ops = sequence(dataStack(push(4)), dataStack(push(2)), dataStack(push(8)), `>R`)
    val ctx = ops.runS(emptyContext).get
    ctx.rs shouldEqual List(8)
    ctx.ds shouldEqual List(2, 4)
  }

  test("should move value from return stack to data stack") {
    val ops = sequence(returnStack(push(4)), dataStack(push(2)), dataStack(push(8)), `R>`)
    val ctx = ops.runS(emptyContext).get
    ctx.rs shouldEqual List.empty
    ctx.ds shouldEqual List(4, 8, 2)
  }

  test("should copy value from return stack to data stack") {
    val ops = sequence(returnStack(push(4)), dataStack(push(2)), dataStack(push(8)), `R@`)
    val ctx = ops.runS(emptyContext).get
    ctx.rs shouldEqual List(4)
    ctx.ds shouldEqual List(4, 8, 2)
  }

  test("should pick a value") {
    val data = List(3, 5, 2, 16, 1, 27, 2).map(x => dataStack(push(x)))
    val ops = for {
      _ <- sequence(data: _*)
      _ <- dataStack(push(3))
      _ <- PICK
    } yield ()

    val ctx = ops.runS(emptyContext).get
    ctx.ds shouldEqual List(16, 2, 27, 1, 16, 2, 5, 3)
  }

  test("should throw error -11 when picking non-existant value") {
    val data = List(3, 5, 2, 16, 1, 27, 2).map(x => dataStack(push(x)))
    val ops = for {
      _ <- sequence(data: _*)
      _ <- dataStack(push(199))
      _ <- PICK
    } yield ()

    ops.runS(emptyContext).failed.get shouldEqual Error(-11)
  }
}