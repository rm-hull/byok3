package byok3

import byok3.data_structures.{Context, Error}
import cats.implicits._
import org.scalatest.FunSpec

class InterpreterTest extends FunSpec {

  val emptyContext = Context(0x10000)

  describe("Interpreter") {
    it("should push values onto the data stack") {
      val ctx = Interpreter("9  5 3").runS(emptyContext).get
      assert(ctx.ds == List(3, 5, 9))
    }

    it("should execute primitives sequentially") {
      val ctx = Interpreter(" 3 DUP * 2 -").runS(emptyContext).get
      assert(ctx.ds == List(7))
    }

    it("should perform subtraction properly") {
      val ctx = Interpreter("5 2 -").runS(emptyContext).get
      assert(ctx.ds == List(3))
    }

    it("should perform division properly") {
      val ctx = Interpreter("10 2 /").runS(emptyContext).get
      assert(ctx.ds == List(5))
    }

    it("should record an error when stack underflow occurs") {
      val ctx = Interpreter("10 +").runS(emptyContext).get
      assert(ctx.status == Error(-4))
      assert(ctx.ds == List.empty)
      assert(ctx.rs == List.empty)
    }

    it("should record an error when accessing invalid memory") {
      val ctx = Interpreter("-2 @").runS(emptyContext).get
      assert(ctx.status == Error(-9, "-2"))
      assert(ctx.ds == List.empty)
      assert(ctx.rs == List.empty)
    }

    it("should create a constant") {
      val ops = for {
        _ <- Interpreter("220 CONSTANT LIMIT")
        _ <- Interpreter("20 LIMIT +")
      } yield ()

      val ctx = ops.runS(emptyContext).get
      assert(ctx.ds == List(240))
    }

    it("should create a variable") {
      val ops = for {
        _ <- Interpreter("VARIABLE DATE")
        _ <- Interpreter("12 DATE !")
        _ <- Interpreter("DATE @ 3 +")
      } yield ()

      val ctx = ops.runS(emptyContext).get
      assert(ctx.ds == List(15))
      assert(ctx.mem.peek(0x100) == 12)
    }

    it("should parse the input stream") {
      val ops = Interpreter("33 PARSE BEYOND SPACE! 42")
      val ctx = ops.runS(emptyContext).get
      assert(ctx.ds == List(42, 9, 12))

      Stream.from(9).zip("BEYOND SPACE").foreach {
        case (addr, ch) => assert(ctx.mem.peek(addr) === ch)
      }
    }

    it("should process a large input stream") {
      val n = 5000
      val data = Range(0, n).map(i => s"$i DROP").mkString(" ") + s" $n"
      val ctx = Interpreter(data).runS(emptyContext).get
      assert(ctx.ds == List(n))
    }
    
    it("should record an error on unfound word") {
      val ctx = Interpreter("10 4 + SAUSAGES 19 4 -").runS(emptyContext).get
      assert(ctx.status == Error(-13, "SAUSAGES"))
      assert(ctx.ds == List.empty)
      assert(ctx.rs == List.empty)
    }

    it("should record an error when user-defined throw occurs") {
      val ctx = Interpreter("10 4 + THROW 19 4 -").runS(emptyContext).get
      assert(ctx.status == Error(14))
      assert(ctx.ds == List.empty)
      assert(ctx.rs == List.empty)
    }
  }
}
