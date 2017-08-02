package byok3.data_structures

import byok3.data_structures.Stack._
import byok3.helpers._
import cats.implicits._
import org.scalatest.{FunSuite, Matchers}


class StackTest extends FunSuite with Matchers {


  test("should push to an empty stack") {
    val ops = sequence(push(10), push(3), push(6))
    ops.runS(List.empty).get shouldEqual List(6, 3, 10)
  }

  test("should push to an existing stack") {
    val stack = List(6, 3, 10)
    val ops = sequence(push(19), push(12))
    ops.runS(stack).get shouldEqual List(12, 19, 6, 3, 10)
  }

  test("should peek the top element on the stack") {
    val ops = for {
      _ <- push(4)
      _ <- push(7)
      _ <- push(9)
      a <- peek
    } yield a
    ops.runA(List.empty).get shouldEqual 9
  }

  test("should fail when peeking an empty stack") {
    intercept[NoSuchElementException] {
      peek.run(List.empty).get
    }
  }

  test("should pop in the reverse order") {
    val ops = for {
      _ <- push(4)
      _ <- push(7)
      _ <- push(9)
      a <- pop
      b <- pop
      c <- pop
    } yield (a, b, c)

    ops.run(List.empty).get shouldEqual(List.empty, (9, 7, 4))
  }

  test("should fail when popping an empty stack") {
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