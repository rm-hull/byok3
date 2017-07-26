package byok3

  import byok3.helpers._
import byok3.data_structures.Error
import byok3.data_structures.Stack._
import org.scalatest.FunSpec
import cats.implicits._

import scala.util.{Failure, Success}


class StackTest extends FunSpec {

  describe("Push") {
    it("should append to an empty stack") {
      val ops = sequence(push(10), push(3), push(6))
      assert(ops.run(List.empty).map(_._1) === Success(List(6, 3, 10)))
    }

    it("should append to an existing stack") {
      val stack = List(6, 3, 10)
      val ops = sequence(push(19), push(12))
      assert(ops.run(stack).map(_._1) === Success(List(12, 19, 6, 3, 10)))
    }
  }

  describe("Peek") {
    it("should peek the top element on the stack") {
      val ops = for {
        _ <- push(4)
        _ <- push(7)
        _ <- push(9)
        a <- peek
      } yield a
      assert(ops.run(List.empty).map(_._2) === Success(9))
    }
    it("should fail when stack is empty") {
      intercept[NoSuchElementException] {
        peek.run(List.empty).get
      }
    }
  }

  describe("Pop") {
    it("should pop in the reverse order") {
      val ops = for {
        _ <- push(4)
        _ <- push(7)
        _ <- push(9)
        a <- pop
        b <- pop
        c <- pop
      } yield (a, b, c)

      assert(ops.run(List.empty) === Success(List.empty, (9, 7, 4)))
    }

    it("should fail when stack is empty") {
      val ops = for {
        _ <- push(4)
        _ <- push(7)
        a <- pop
        b <- pop
        c <- pop
      } yield (a, b, c)

      intercept[NoSuchElementException] {
        ops.run(List.empty).get
      }
    }
  }
}