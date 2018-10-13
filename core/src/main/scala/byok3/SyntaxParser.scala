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


import byok3.SyntaxTokens.{LastToken, SyntaxToken}
import byok3.data_structures._
import byok3.types.Word
import org.parboiled2._
import byok3.implicits._
import shapeless.HNil

import scala.util.Try

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
  private val NO_SPACE = CharPredicate.Printable -- " "

  private val dictionary = ctx.dictionary.toMap.iterator.map {
    case (word, exeTok) => (word.toLowerCase, SyntaxTokens.DictionaryWord(exeTok))
  }.toMap

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
    "\"" | ".\"" | ignoreCase("abort\"") | ignoreCase("warning\"")
  }

  private def StringLiteral = rule {
    capture(
      StringStart ~ EOI |
        StringStart ~ " " ~ zeroOrMore(STRING_BASE) ~ "\"" |
        StringStart ~ " " ~ zeroOrMore(STRING_BASE) ~ EOI
    ) ~> SyntaxTokens.StringLiteral
  }

  private def NumberLiteral = rule {
    capture(optional('#') ~ optional('-') ~ oneOrMore(CharPredicate.Digit)) ~> SyntaxTokens.NumberLiteral |
    capture('$' ~ optional('-') ~ oneOrMore(anyOf("01234567890abcdefABCDEF"))) ~> SyntaxTokens.NumberLiteral |
    capture('%' ~ optional('-') ~ oneOrMore(anyOf("01"))) ~> SyntaxTokens.NumberLiteral
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
}
