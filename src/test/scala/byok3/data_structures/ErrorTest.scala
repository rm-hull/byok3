package byok3.data_structures

import org.scalatest.{FunSuite, Matchers}

import scala.util.Try

class ErrorTest extends FunSuite with Matchers {

  test("should transform divide by zero to error -10") {
    val ex = Try(1 / 0).failed.get
    Error(ex) shouldEqual Error(-10)
  }

  test("should transform index out of bounds to error -9") {
    val ex = Try(List.empty(17)).failed.get
    Error(ex) shouldEqual Error(-9, "17")
  }

  test("should transform no such element to error -4") {
    val ex = Try(List.empty.head).failed.get
    Error(ex) shouldEqual Error(-4)
  }

  test("should not transform existing error") {
    Error(Error(342)) shouldEqual Error(342)
  }

  test("should transform uncaught throwable to error 0") {
    Error(new RuntimeException("unit test err")) shouldEqual Error(0, "[java.lang.RuntimeException] unit test err")
  }
}
