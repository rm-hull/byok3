package byok3

import byok3.data_structures.{Context, Error}
import cats.implicits._
import org.scalatest.{FunSuite, Matchers}

class InterpreterTest extends FunSuite with Matchers {

  val emptyContext = Context(0x10000)

  test("should push values onto the data stack") {
    val ctx = Interpreter("9  5 3").runS(emptyContext).get
    ctx.ds shouldEqual List(3, 5, 9)
  }

  test("should execute primitives sequentially") {
    val ctx = Interpreter(" 3 DUP * 2 -").runS(emptyContext).get
    ctx.ds shouldEqual List(7)
  }

  test("should perform subtraction properly") {
    val ctx = Interpreter("5 2 -").runS(emptyContext).get
    ctx.ds shouldEqual List(3)
  }

  test("should perform division properly") {
    val ctx = Interpreter("10 2 /").runS(emptyContext).get
    ctx.ds shouldEqual List(5)
  }

  test("should record an error when stack underflow occurs") {
    val ctx = Interpreter("10 +").runS(emptyContext).get
    assert(ctx.status == Left(Error(-4)))
    ctx.ds shouldEqual List.empty
    ctx.rs shouldEqual List.empty
  }

  test("should record an error when accessing invalid memory") {
    val ctx = Interpreter("-2 @").runS(emptyContext).get
    assert(ctx.status == Left(Error(-9, "0xFFFFFFFE")))
    ctx.ds shouldEqual List.empty
    ctx.rs shouldEqual List.empty
  }

  test("should create a constant") {
    val ops = for {
      _ <- Interpreter("220 CONSTANT LIMIT")
      _ <- Interpreter("20 LIMIT +")
    } yield ()

    val ctx = ops.runS(emptyContext).get
    ctx.ds shouldEqual List(240)
  }

  test("should create a variable") {
    val ops = for {
      _ <- Interpreter("VARIABLE DATE")
      _ <- Interpreter("12 DATE !")
      _ <- Interpreter("DATE @ 3 +")
    } yield ()

    val ctx = ops.runS(emptyContext).get
    ctx.ds shouldEqual List(15)
  }

  test("should parse the input stream") {
    val ops = Interpreter("33 PARSE BEYOND SPACE! 42")
    val ctx = ops.runS(emptyContext).get
    ctx.ds shouldEqual List(42, 12, 9)

    Stream.from(9).zip("BEYOND SPACE").foreach {
      case (addr, ch) => ctx.mem.char_peek(addr) shouldEqual ch
    }
  }

  test("should record an error on unfound word") {
    val ctx = Interpreter("10 4 + SAUSAGES 19 4 -").runS(emptyContext).get
    ctx.status shouldEqual Left(Error(-13, "SAUSAGES"))
    ctx.ds shouldEqual List.empty
    ctx.rs shouldEqual List.empty
  }

  test("should record an error when user-defined throw occurs") {
    val ctx = Interpreter("10 4 + THROW 19 4 -").runS(emptyContext).get
    ctx.status shouldEqual Left(Error(14))
    ctx.ds shouldEqual List.empty
    ctx.rs shouldEqual List.empty
  }

  test("should print the top stack item") {
    val ctx = Interpreter("1 2 3 5 7 + . . .").runS(emptyContext).get
    ctx.ds shouldEqual List(1)
    assertOutput(ctx.io)("12 3 2 ")
  }

  test("should print the stack") {
    val ctx = Interpreter("1 2 3 5 7 + .S").runS(emptyContext).get
    ctx.ds shouldEqual List(12, 3, 2, 1)
    assertOutput(ctx.io)("1 2 3 12 ")
  }

  test("should parse and print the message") {
    val ctx = Interpreter("33 PARSE HELLO, WORLD! TYPE 10 EMIT 33 PARSE THAT IS ALL! TYPE").runS(emptyContext).get
    assertOutput(ctx.io)("HELLO, WORLD\nTHAT IS ALL")
  }

  test("should compile and run a user defined word") {
    val ops = for {
      _ <- Interpreter(": SQR DUP * ;")
      _ <- Interpreter("3 SQR")
    } yield ()

    val ctx = ops.runS(emptyContext).get
    ctx.ds shouldEqual List(9)
  }

  test("should compile and run a user defined word over several sessions") {
    val ops = for {
      _ <- Interpreter(": SQR ")
      _ <- Interpreter("DUP * ;")
      _ <- Interpreter("3 SQR")
    } yield ()

    val ctx = ops.runS(emptyContext).get
    ctx.ds shouldEqual List(9)
  }

  test("should record error when LITERAL when not in compile mode") {
    val ctx = Interpreter("10 LITERAL").runS(emptyContext).get
    ctx.status shouldEqual Left(Error(-14))
  }

}
