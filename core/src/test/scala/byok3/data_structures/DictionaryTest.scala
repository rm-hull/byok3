/*
 * Copyright (c) 2017 Richard Hull
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package byok3.data_structures

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers


class DictionaryTest extends AnyFunSuite with Matchers {

  private val dict = Range(0, 10).foldLeft[Dictionary[String, Int]](Dictionary.empty) {
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