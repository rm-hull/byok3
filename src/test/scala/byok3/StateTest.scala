package byok3

import org.scalatest.FunSpec

trait RNG {
  def nextInt: (Int, RNG)
}

case class ConstantRNG(value: Int) extends RNG {
  override def nextInt: (Int, RNG) = {
    (value, ConstantRNG(value))
  }
}

case class SequentialRNG(value: Int) extends RNG {
  override def nextInt: (Int, RNG) = {
    (value, SequentialRNG(value + 1))
  }
}

class StateTest extends FunSpec {

  describe("State") {
    type Rand[A] = State[RNG, A]

    def plus1: Rand[Int] =
      State(rng => rng.nextInt match {
        case (i, next) => (i + 1, next)
      })

    it("should map") {
      assert(State.unit(2).map(_ + 1).run(SequentialRNG(5))._1 === 3)
    }

    it("should map2") {
      assert(State.unit(2).map2(plus1)(_ + _).run(ConstantRNG(5))._1 === 8)
    }

    it("should sequence a list of Rand[A]") {
      assert(State.sequence(List.fill(5)(plus1)).run(SequentialRNG(-2))._1 === List(-1, 0, 1, 2, 3))
    }
  }

}
