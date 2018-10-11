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

package byok3

import byok3.SyntaxTokens._
import byok3.data_structures.Constant
import org.scalatest.{FunSuite, Matchers}

import scala.util.Success

class SyntaxParserTest extends FunSuite with Matchers {

  test("should parse spaces as whitespace") {
    new SyntaxParser("   ").InputLine.run() shouldEqual
      Success(List(Whitespace("   ")))
  }

  test("should parse newlines as whitespace") {
    new SyntaxParser("  \n ").InputLine.run() shouldEqual
      Success(List(Whitespace("  \n ")))
  }

  test("should parse a forth comment as a comment") {
    new SyntaxParser("  ( hello world) ").InputLine.run() shouldEqual
      Success(List(
        Whitespace("  "),
        Comment("( hello world)"),
        Whitespace(" ")
      ))
  }

  test("should parse a single open paren as a comment") {
    new SyntaxParser("  (").InputLine.run() shouldEqual
      Success(List(
        Whitespace("  "),
        Comment("(")
      ))
  }

  test("should parse an open forth comment to the end of input") {
    new SyntaxParser("  ( hello world  ").InputLine.run() shouldEqual
      Success(List(
        Whitespace("  "),
        Comment("( hello world  ")
      ))
  }

  test("should embed string in comment") {
    new SyntaxParser("  ( hello \" big \" world  )").InputLine.run() shouldEqual
      Success(List(
        Whitespace("  "),
        Comment("( hello \" big \" world  )")
      ))
  }

  test("should parse a forth dot-comment as a comment") {
    new SyntaxParser("  .( goodbye ) ").InputLine.run() shouldEqual
      Success(List(
        Whitespace("  "),
        Comment(".( goodbye )"),
        Whitespace(" ")
      ))
  }

  test("should parse a forth backslash-comment as a comment") {
    new SyntaxParser("  \\ so far so good").InputLine.run() shouldEqual
      Success(List(
        Whitespace("  "),
        Comment("\\ so far so good")
      ))
  }

  test("should parse a forth string as a string") {
    new SyntaxParser("  \" hello world\" ").InputLine.run() shouldEqual
      Success(List(
        Whitespace("  "),
        StringLiteral("\" hello world\""),
        Whitespace(" ")
      ))
  }

  test("should embed parens in a string") {
    new SyntaxParser("  \" hello ( cruel ) world\" ").InputLine.run() shouldEqual
      Success(List(
        Whitespace("  "),
        StringLiteral("\" hello ( cruel ) world\""),
        Whitespace(" ")
      ))
  }

  test("should parse a single open double quote as a string") {
    new SyntaxParser("  \"").InputLine.run() shouldEqual
      Success(List(
        Whitespace("  "),
        StringLiteral("\"")
      ))
  }

  test("should parse an open string to the end of input") {
    new SyntaxParser("  \" hello world  ").InputLine.run() shouldEqual
      Success(List(
        Whitespace("  "),
        StringLiteral("\" hello world  ")
      ))
  }

  test("should parse a forth dot-string as a comment") {
    new SyntaxParser("  .\" goodbye \" ").InputLine.run() shouldEqual
      Success(List(
        Whitespace("  "),
        StringLiteral(".\" goodbye \""),
        Whitespace(" ")
      ))
  }

  test("should parse a number literal") {
    new SyntaxParser("  3145 ").InputLine.run() shouldEqual
      Success(List(
        Whitespace("  "),
        NumberLiteral("3145"),
        Whitespace(" ")
      ))
  }

  test("should parse dictionary words") {
    val words = Set("HELLO", "WORLD")
    new SyntaxParser("  hello WORLD ", words).InputLine.run() shouldEqual
      Success(List(
        Whitespace("  "),
        DictionaryWord("HELLO", Constant("HELLO", 45)),
        Whitespace(" "),
        DictionaryWord("WORLD", Constant("WORLD", 45)),
        Whitespace(" ")
      ))
  }

  test("should handle unknown token") {
    val words = Set("HELLO", "WORLD")
    new SyntaxParser(" HELLO GOOD\tBYE WORLD", words).InputLine.run() shouldEqual
      Success(List(
        Whitespace(" "),
        DictionaryWord("HELLO", Constant("HELLO", 45)),
        Whitespace(" "),
        Unknown("GOOD"),
        Whitespace("\t"),
        Unknown("BYE"),
        Whitespace(" "),
        DictionaryWord("WORLD", Constant("WORLD", 45))
      ))
  }


  test("should handle last token") {
    val words = Set("HELLO")
    new SyntaxParser(" HELLO GOOD\tBYE WORLD", words).InputLine.run() shouldEqual
      Success(List(
        Whitespace(" "),
        DictionaryWord("HELLO", Constant("HELLO", 45)),
        Whitespace(" "),
        Unknown("GOOD"),
        Whitespace("\t"),
        Unknown("BYE"),
        Whitespace(" "),
        LastToken("WORLD")
      ))
  }

  test("should handle simple definition token") {
    val words = Set("DUP", "*", ";")
    new SyntaxParser(": SQUARE ( n -- n ) DUP * ;", words).InputLine.run() shouldEqual
      Success(List(
        Definition(": SQUARE"),
        Whitespace(" "),
        Comment("( n -- n )"),
        Whitespace(" "),
        DictionaryWord("DUP", Constant("DUP", 45)),
        Whitespace(" "),
        DictionaryWord("*", Constant("*", 45)),
        Whitespace(" "),
        DictionaryWord(";", Constant(";", 45)),
      ))
  }

  println(Darkula(": SQUARE ( n -- n ) DUP * ;", Set("DUP", "*", ";")))
}

