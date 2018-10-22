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


import byok3.SyntaxTokens.SyntaxToken
import byok3.data_structures._
import cats.implicits._
import org.parboiled2._

import scala.util.{Success, Try}

object SyntaxTokens {

  sealed trait SyntaxToken

  case class Whitespace(value: String) extends SyntaxToken
  case class Comment(value: String) extends SyntaxToken
  case class StringLiteral(value: String) extends SyntaxToken
  case class NumberLiteral(value: String) extends SyntaxToken
  case class DictionaryWord(exeTok: ExecutionToken) extends SyntaxToken
  case class LastToken(value: String) extends SyntaxToken
  case class Unknown(value: String) extends SyntaxToken
  case class Definition(value: String) extends SyntaxToken

}

//private class DictionaryMap(words: Set[Word]) extends Map[String, SyntaxTokens.DictionaryWord] {
//
//
//  override def get(key: String): Option[SyntaxTokens.DictionaryWord] =
//    if (words.contains(key.toUpperCase)) Some(SyntaxTokens.DictionaryWord(key)) else None
//
//  override def iterator: Iterator[(String, SyntaxTokens.DictionaryWord)] =
//    words.iterator.map(key => (key.toUpperCase, SyntaxTokens.DictionaryWord(key)))
//
//  override def +[V1 >: SyntaxTokens.DictionaryWord](kv: (String, V1)): Map[String, V1] =
//    new DictionaryMap(words + kv._1)
//
//  override def -(key: String): Map[String, SyntaxTokens.DictionaryWord] =
//    new DictionaryMap(words - key)
//}

class SyntaxParser(val input: ParserInput, ctx: Context) extends Parser {
  private val COMMENT_BASE = CharPredicate.Printable -- ")"
  private val STRING_BASE = CharPredicate.Printable -- "\""
  private val STRING_BASE_NO_QUOTE = CharPredicate.Printable -- "'"
  private val NO_SPACE = CharPredicate.Printable -- " "

  private val dictionary = ctx.dictionary.toMap.iterator.map {
    case (word, exeTok) => (word.toLowerCase, SyntaxTokens.DictionaryWord(exeTok))
  }.toMap

  private val base = Context.deref("BASE").runA(ctx)

  def InputLine = rule {
    // ((Tokens ~ optional(LastToken)) ~> ((lst:Seq[SyntaxToken], x:Option[LastToken]) => lst ++ x.toList)) ~ EOI
    Tokens ~ EOI
  }

  private def Tokens: Rule1[Seq[SyntaxToken]] = rule {
    (optional(Token) ~ Whitespace ~ Tokens) ~> ((x1:Option[SyntaxToken], x2:SyntaxToken, x3:Seq[SyntaxToken]) => x1.toList ++ (x2 +: x3)) |
      (Whitespace ~ optional(Token)) ~> ((x1:SyntaxToken, x2:Option[SyntaxToken]) => x1 +: x2.toList) |
      (Token ~ optional(Whitespace)) ~> ((x1:SyntaxToken, x2:Option[SyntaxToken]) => x1 +: x2.toList)
  }

  private def Token = rule {
    Comment | StringLiteral | Definition | DictionaryWord | NumberLiteral | LastToken | Unknown
  }

  private def NewLine = rule {
    optional('\r') ~ '\n'
  }

  private def Whitespace = rule {
    capture(oneOrMore(" " | "\t" | NewLine)) ~> SyntaxTokens.Whitespace
  }

  private def Comment = rule {
    capture(
      optional('.') ~ '(' ~ EOI |
        optional('.') ~ "( " ~ zeroOrMore(COMMENT_BASE) ~ ")" |
        optional('.') ~ "( " ~ zeroOrMore(COMMENT_BASE) ~ EOI |
        "\\" ~ (EOI | test(cursorChar == '\n')) |
        "\\ " ~ zeroOrMore(CharPredicate.Printable) ~ (EOI | test(cursorChar == '\n'))
    ) ~> SyntaxTokens.Comment
  }

  private def StringStart = rule {
    "\"" | ".\"" | ignoreCase("abort\"") | ignoreCase("warning\"") | ignoreCase("p\"") | ignoreCase("c\"") | ignoreCase("s\"")
  }

  private def StringLiteral = rule {
    capture(
      StringStart ~ EOI |
        ".'" ~ EOI |
        ".'" ~ zeroOrMore(STRING_BASE_NO_QUOTE) ~ "'" |
        ".'" ~ zeroOrMore(STRING_BASE_NO_QUOTE) ~ EOI |
        StringStart ~ " " ~ zeroOrMore(STRING_BASE) ~ "\"" |
        StringStart ~ " " ~ zeroOrMore(STRING_BASE) ~ EOI
    ) ~> SyntaxTokens.StringLiteral
  }

  private def NumberLiteral = rule {
    capture(optional('-') ~ oneOrMore(anyOf("01234567890abcdefABCDEF"))) ~> asNumber(base) _ |
      capture('#' ~ optional('-') ~ oneOrMore(CharPredicate.Digit)) ~> asNumber(10, 1) _ |
      capture('$' ~ optional('-') ~ oneOrMore(anyOf("01234567890abcdefABCDEF"))) ~> asNumber(16, 1) _ |
      capture('%' ~ optional('-') ~ oneOrMore(anyOf("01"))) ~> asNumber(2, 1) _
  }
  
  private def DictionaryWord = rule {
     valueMap(dictionary, ignoreCase = true) ~ (EOI | test(Set(' ', '\t', '\n').contains(cursorChar)))
  }

  private def LastToken = rule {
    capture(oneOrMore(NO_SPACE)) ~ EOI ~> SyntaxTokens.LastToken
  }

  private def Definition = rule {
    capture(":" ~ oneOrMore(" " | "\t'" | NewLine) ~ zeroOrMore(NO_SPACE) | ":" ~ EOI) ~> SyntaxTokens.Definition
  }

  private def Unknown = rule {
    capture(oneOrMore(NO_SPACE)) ~> SyntaxTokens.Unknown
  }

  private def asNumber(radix: Try[Int], offset: Int = 0)(text: String): Rule1[SyntaxTokens.NumberLiteral] = rule {
    test(canParseAsNumber(radix, text.substring(offset))) ~ push(SyntaxTokens.NumberLiteral(text))
  }

  private def asNumber(radix: Int, offset: Int)(text: String): Rule1[SyntaxTokens.NumberLiteral] = rule {
    asNumber(Success(radix), offset)(text)
  }

  private def canParseAsNumber(radix: Try[Int], text: String) =
    radix.flatMap(r => Try(Integer.parseInt(text, r))).isSuccess
}
