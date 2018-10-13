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

import byok3.SyntaxTokens.{Whitespace, _}
import byok3.data_structures.Source.USER_INPUT_DEVICE
import org.parboiled2.ParseError
import org.scalatest.{FunSuite, Matchers}

import scala.util.Success

class SyntaxParserTest extends FunSuite with Matchers {

  test("should parse spaces as whitespace") {
    new SyntaxParser("   ", emptyContext).InputLine.run() shouldEqual
      Success(List(Whitespace("   ")))
  }

  test("should parse newlines as whitespace") {
    new SyntaxParser("  \n ", emptyContext).InputLine.run() shouldEqual
      Success(List(Whitespace("  \n ")))
  }

  test("should parse a forth comment") {
    new SyntaxParser("  ( hello world) ", emptyContext).InputLine.run() shouldEqual
      Success(List(
        Whitespace("  "),
        Comment("( hello world)"),
        Whitespace(" ")
      ))
  }

  test("should parse a single open paren") {
    new SyntaxParser("  (", emptyContext).InputLine.run() shouldEqual
      Success(List(
        Whitespace("  "),
        Comment("(")
      ))
  }

  test("should parse an open forth comment to the end of input") {
    new SyntaxParser("  ( hello world  ", emptyContext).InputLine.run() shouldEqual
      Success(List(
        Whitespace("  "),
        Comment("( hello world  ")
      ))
  }

  test("should embed string in comment") {
    new SyntaxParser("  ( hello \" big \" world  )", emptyContext).InputLine.run() shouldEqual
      Success(List(
        Whitespace("  "),
        Comment("( hello \" big \" world  )")
      ))
  }

  test("should parse a forth dot-comment as a comment") {
    new SyntaxParser("  .( goodbye ) ", emptyContext).InputLine.run() shouldEqual
      Success(List(
        Whitespace("  "),
        Comment(".( goodbye )"),
        Whitespace(" ")
      ))
  }

  test("should parse a forth backslash-comment as a comment") {
    new SyntaxParser("  \\ so far so good", emptyContext).InputLine.run() shouldEqual
      Success(List(
        Whitespace("  "),
        Comment("\\ so far so good")
      ))
  }

  test("should parse a forth string as a string") {
    new SyntaxParser("  \" hello world\" ", emptyContext).InputLine.run() shouldEqual
      Success(List(
        Whitespace("  "),
        StringLiteral("\" hello world\""),
        Whitespace(" ")
      ))
  }

  test("should embed parens in a string") {
    new SyntaxParser("  \" hello ( cruel ) world\" ", emptyContext).InputLine.run() shouldEqual
      Success(List(
        Whitespace("  "),
        StringLiteral("\" hello ( cruel ) world\""),
        Whitespace(" ")
      ))
  }

  test("should parse a single open double quote as a string") {
    new SyntaxParser("  \"", emptyContext).InputLine.run() shouldEqual
      Success(List(
        Whitespace("  "),
        StringLiteral("\"")
      ))
  }

  test("should parse an open string to the end of input") {
    new SyntaxParser("  \" hello world  ", emptyContext).InputLine.run() shouldEqual
      Success(List(
        Whitespace("  "),
        StringLiteral("\" hello world  ")
      ))
  }

  test("should parse a forth dot-string as a comment") {
    new SyntaxParser("  .\" goodbye \" ", emptyContext).InputLine.run() shouldEqual
      Success(List(
        Whitespace("  "),
        StringLiteral(".\" goodbye \""),
        Whitespace(" ")
      ))
  }

  test("should parse a number literal") {
    new SyntaxParser("  3145 ", emptyContext).InputLine.run() shouldEqual
      Success(List(
        Whitespace("  "),
        NumberLiteral("3145"),
        Whitespace(" ")
      ))
  }

  test("should parse a negative number literal") {
    new SyntaxParser("-94", emptyContext).InputLine.run() shouldEqual
      Success(List(
        NumberLiteral("-94")
      ))
  }

  test("should parse dictionary words") {
    val ctx = emptyContext
      .eval("10 CONSTANT HELLO", source = USER_INPUT_DEVICE)
      .eval("20 CONSTANT WORLD", source = USER_INPUT_DEVICE)

    new SyntaxParser("  hello  WORLD ", ctx).InputLine.run() shouldEqual
      Success(List(
        Whitespace("  "),
        DictionaryWord(ctx.dictionary("HELLO")),
        Whitespace("  "),
        DictionaryWord(ctx.dictionary("WORLD")),
        Whitespace(" ")
      ))
  }

  test("should handle unknown token") {
    val ctx = emptyContext
      .eval("10 CONSTANT HELLO", source = USER_INPUT_DEVICE)
      .eval("20 CONSTANT WORLD", source = USER_INPUT_DEVICE)

    new SyntaxParser("WORLD  HELLO GOOD\tBYE WORLD", ctx).InputLine.run() shouldEqual
      Success(List(
        DictionaryWord(ctx.dictionary("WORLD")),
        Whitespace("  "),
        DictionaryWord(ctx.dictionary("HELLO")),
        Whitespace(" "),
        Unknown("GOOD"),
        Whitespace("\t"),
        DictionaryWord(ctx.dictionary("BYE")),
        Whitespace(" "),
        DictionaryWord(ctx.dictionary("WORLD")),
      ))
  }

  test("should handle dictionary word in isolation") {
    val ctx = emptyContext
      .eval("10 CONSTANT HELLO", source = USER_INPUT_DEVICE)
      .eval("20 CONSTANT WORLD", source = USER_INPUT_DEVICE)

    new SyntaxParser("WORLD", ctx).InputLine.run() shouldEqual
      Success(List(
        DictionaryWord(ctx.dictionary("WORLD"))
      ))
  }

  test("should handle single last token") {
    new SyntaxParser("  HELLO", emptyContext).InputLine.run() shouldEqual
      Success(List(
        Whitespace("  "),
        LastToken("HELLO")
      ))
  }

  test("should handle last token") {
    val ctx = emptyContext
      .eval("10 CONSTANT HELLO", source = USER_INPUT_DEVICE)

    new SyntaxParser("HELLO GOODBYE HELL", ctx).InputLine.run() shouldEqual
      Success(List(
        DictionaryWord(ctx.dictionary("HELLO")),
        Whitespace(" "),
        Unknown("GOODBYE"),
        Whitespace(" "),
        LastToken("HELL")
      ))
  }

  test("should handle simple definition token") {
    val ctx = emptyContext
    new SyntaxParser(": SQUARE ( n -- n ) DUP * ;", ctx).InputLine.run() shouldEqual
      Success(List(
        Definition(": SQUARE"),
        Whitespace(" "),
        Comment("( n -- n )"),
        Whitespace(" "),
        DictionaryWord(ctx.dictionary("DUP")),
        Whitespace(" "),
        DictionaryWord(ctx.dictionary("*")),
        Whitespace(" "),
        DictionaryWord(ctx.dictionary(";")),
      ))
  }

  test("should handle SQRT-CLOSER definition") {
    val ctx = emptyContext
    val defn = ": sqrt-closer  ( square guess -- square guess adjustment ) 2dup / over - 2 / ;"
    new SyntaxParser(defn, ctx).InputLine.run() shouldEqual
      Success(List(
        Definition(": sqrt-closer"),
        Whitespace("  "),
        Comment("( square guess -- square guess adjustment )"),
        Whitespace(" "),
        DictionaryWord(ctx.dictionary("2DUP")),
        Whitespace(" "),
        DictionaryWord(ctx.dictionary("/")),
        Whitespace(" "),
        DictionaryWord(ctx.dictionary("OVER")),
        Whitespace(" "),
        DictionaryWord(ctx.dictionary("-")),
        Whitespace(" "),
        NumberLiteral("2"),
        Whitespace(" "),
        DictionaryWord(ctx.dictionary("/")),
        Whitespace(" "),
        DictionaryWord(ctx.dictionary(";"))
      ))
  }

  test("should handle DOES> definition") {
    val ctx = emptyContext
    val defn =
      """: DOES>   ( -- , define execution code for CREATE word )
        |        0 [compile] literal \ dummy literal to hold xt
        |        here cell-          \ address of zero in literal
        |        compile (does>)     \ call (DOES>) from new creation word
        |        >r                  \ move addrz to return stack so ; doesn't see stack garbage
        |        [compile] ;         \ terminate part of code before does>
        |        r>
        |        :noname       ( addrz xt )
        |        compile rdrop       \ drop a stack frame (call becomes goto)
        |        swap !              \ save execution token in literal
        |; immediate
        |""".stripMargin
    new SyntaxParser(defn, ctx).InputLine.run() shouldEqual Success(List(
      Definition(": DOES>"),
      Whitespace("   "),
      Comment("( -- , define execution code for CREATE word )"),

      Whitespace("\n        "),
      NumberLiteral("0"),
      Whitespace(" "),
      DictionaryWord(ctx.dictionary("[COMPILE]")),
      Whitespace(" "),
      DictionaryWord(ctx.dictionary("LITERAL")),
      Whitespace(" "),
      Comment("\\ dummy literal to hold xt"),

      Whitespace("\n        "),
      DictionaryWord(ctx.dictionary("HERE")),
      Whitespace(" "),
      DictionaryWord(ctx.dictionary("CELL-")),
      Whitespace("          "),
      Comment("\\ address of zero in literal"),

      Whitespace("\n        "),
      DictionaryWord(ctx.dictionary("COMPILE")),
      Whitespace(" "),
      DictionaryWord(ctx.dictionary("(DOES>)")),
      Whitespace("     "),
      Comment("\\ call (DOES>) from new creation word"),

      Whitespace("\n        "),
      DictionaryWord(ctx.dictionary(">R")),
      Whitespace("                  "),
      Comment("\\ move addrz to return stack so ; doesn't see stack garbage"),

      Whitespace("\n        "),
      DictionaryWord(ctx.dictionary("[COMPILE]")),
      Whitespace(" "),
      DictionaryWord(ctx.dictionary(";")),
      Whitespace("         "),
      Comment("\\ terminate part of code before does>"),

      Whitespace("\n        "),
      DictionaryWord(ctx.dictionary("R>")),

      Whitespace("\n        "),
      DictionaryWord(ctx.dictionary(":NONAME")),
      Whitespace("       "),
      Comment("( addrz xt )"),

      Whitespace("\n        "),
      DictionaryWord(ctx.dictionary("COMPILE")),
      Whitespace(" "),
      DictionaryWord(ctx.dictionary("RDROP")),
      Whitespace("       "),
      Comment("\\ drop a stack frame (call becomes goto)"),

      Whitespace("\n        "),
      DictionaryWord(ctx.dictionary("SWAP")),
      Whitespace(" "),
      DictionaryWord(ctx.dictionary("!")),
      Whitespace("              "),
      Comment("\\ save execution token in literal"),

      Whitespace("\n"),
      DictionaryWord(ctx.dictionary(";")),
      Whitespace(" "),
      DictionaryWord(ctx.dictionary("IMMEDIATE")),
      Whitespace("\n"),
    ))
  }
}