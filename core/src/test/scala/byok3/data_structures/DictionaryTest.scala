package byok3.data_structures

import org.scalatest.{FunSuite, Matchers}

class DictionaryTest extends FunSuite with Matchers {

  val dict = Range(0, 10).foldLeft[Dictionary[String, Int]](Dictionary.empty) {
    (dict, i) => dict.add(i.toString, i)
  }

  test("should have expected length") {
    dict.length shouldEqual 10
  }

  test("should have expected keys") {
    dict.keys shouldEqual Range(0, 10).map(_.toString).toSet
  }

  test("should not have expected entries") {
    dict.get(23) shouldEqual None
    dict.get("A") shouldEqual None
  }

  test("should add entries sequentially") {
    for (i <- Range(0, 10)) {
      dict.get(i) shouldEqual Some(i)
      dict.get(i.toString) shouldEqual Some(i)
      dict.indexOf(i.toString) shouldEqual Some(i)
    }
  }

  test("should shadow old values") {
    val newDict = dict.add("7", 70)
    newDict.get("7") shouldEqual Some(70)
    newDict.get(7) shouldEqual Some(7)
    newDict.indexOf("7") shouldEqual Some(10)
    newDict.get(10) shouldEqual Some(70)
  }

  test("should convert to a map") {
    dict.toMap shouldEqual Range(0, 10).map(i => i.toString -> i).toMap
  }

  test("should forget an existing value") {
    val newDict = dict.forget("3")
    newDict.get("3") shouldEqual None
  }

  test("should remember an old value") {
    val newDict = dict.add("3", 95)
    newDict.get("3") shouldEqual Some(95)
    newDict.forget("3").get("3") shouldEqual Some(3)
  }

  test("should throw error on forgetting a non-existent value") {
    intercept[NoSuchElementException] {
      dict.forget("67")
    }
  }
}