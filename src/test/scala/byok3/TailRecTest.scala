package byok3

import byok3.data_structures.Context
import cats.implicits._
import org.scalatest.{FunSuite, Matchers}

class TailRecTest extends FunSuite with Matchers {

  val emptyContext = Context(0x10000)

  test("should process a large input stream") {
    val n = 5000
    val data = Range(0, n).map(i => s"$i DROP").mkString(" ") + s" $n"
    val ctx = Interpreter(data).runS(emptyContext).get
    ctx.ds shouldEqual List(n)
  }
}
