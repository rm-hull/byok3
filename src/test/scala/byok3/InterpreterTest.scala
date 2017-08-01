package byok3

import byok3.data_structures.{Context, OK}
import cats.implicits._
import org.scalatest.FunSpec

class InterpreterTest extends FunSpec {

  val emptyContext = Context(0x10000)

  describe("Interpreter") {
    it("should push values onto the data stack") {
      val result = Interpreter("9  5 3").runS(emptyContext).get
      assert(result.ds == List(3, 5, 9))
    }

    it("should execute primitives sequentially") {
      val result = Interpreter(" 3 DUP * 2 -").runS(emptyContext).get
      assert(result.ds == List(7))
    }

    it("should perform subtraction properly") {
      val result = Interpreter("5 2 -").runS(emptyContext).get
      assert(result.ds == List(3))
    }

    it("should perform division properly") {
      val result = Interpreter("10 2 /").runS(emptyContext).get
      assert(result.ds == List(5))
    }

    it("should record an error when stack underflow occurs") {
      intercept[NoSuchElementException] {
        Interpreter("10 +").runS(emptyContext).get
      }
    }

    it("should record an error when accessing invalid memory") {
      intercept[IndexOutOfBoundsException] {
        Interpreter("-2 @").runS(emptyContext).get
      }
    }

    it("should create a constant") {
      val ops = for {
        _ <- Interpreter("220 CONSTANT LIMIT")
        _ <- Interpreter("20 LIMIT +")
      } yield ()

      val result = ops.runS(emptyContext).get
      assert(result.ds == List(240))
    }

    it("should create a variable") {
      val ops = for {
        _ <- Interpreter("VARIABLE DATE")
        _ <- Interpreter("12 DATE !")
        _ <- Interpreter("DATE @ 3 +")
      } yield ()

      val result = ops.runS(emptyContext).get
      assert(result.ds == List(15))
      assert(result.mem.peek(0x100) == 12)
    }

    it("should parse the input stream") {
      val ops = Interpreter("33 PARSE BEYOND SPACE! 42")
      val result = ops.runS(emptyContext).get
      assert(result.ds == List(42, 9, 12))

      Stream.from(9).zip("BEYOND SPACE").foreach {
        case (addr, ch) => assert(result.mem.peek(addr) === ch)
      }
    }

    it("should process a large input stream") {
      val n = 5000
      val data = Range(0, n).map(i => s"$i DROP").mkString(" ") + s" $n"
      val result = Interpreter(data).runS(emptyContext).get
      assert(result.ds == List(n))
    }
  }
}
