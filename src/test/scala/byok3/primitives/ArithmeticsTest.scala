package byok3.primitives

import byok3.Stack.push
import byok3.{Sequence, Stack}
import org.scalatest.FunSpec

class ArithmeticsTest extends FunSpec {

  describe("Add") {
    it("should add top two stack elements") {
      val ops = Sequence(push(4), push(7), push(2), Arithmetics.+)
      assert(ops.run(Stack.empty).value._1 === List(9, 4))
    }
  }
}
