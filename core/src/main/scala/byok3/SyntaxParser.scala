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
import byok3.types.Word
import org.parboiled2._
import byok3.implicits._

import scala.util.Try

object SyntaxTokens {

  sealed trait SyntaxToken {
    val value: String
  }

  case class Whitespace(value: String) extends SyntaxToken

  case class Comment(value: String) extends SyntaxToken

  case class StringLiteral(value: String) extends SyntaxToken

  case class NumberLiteral(value: String) extends SyntaxToken

  case class DictionaryWord(value: String, exeTok: ExecutionToken) extends SyntaxToken

  case class LastToken(value: String) extends SyntaxToken

  case class Unknown(value: String) extends SyntaxToken

  case class Definition(value: String) extends SyntaxToken

}

trait ColorTheme {
  def colorize(token: SyntaxToken): String

  def apply(text: String, words: Set[Word]): Try[String] =
    new SyntaxParser(text, words).InputLine.run().map { _.map(colorize).mkString }
}

object Darkula extends ColorTheme {

  import byok3.SyntaxTokens._

  override def colorize(token: SyntaxToken): String = token match {
    case Whitespace(value) => value
    case Comment(value) => value.fg("grey_35")
    case StringLiteral(value) => value.fg("green_4")
    case NumberLiteral(value) => value.fg("deep_sky_blue_2")
    case DictionaryWord(value, _: SystemDefined) => value.fg("dark_orange").bold
    case DictionaryWord(value, _: Primitive) => value.fg("dark_orange").bold
    case DictionaryWord(value, _: Constant) => value.fg("purple_3")
    case DictionaryWord(value, _: Variable) => value.fg("purple_3")
    case DictionaryWord(value, _: UserDefined) => value.fg("white")
    case DictionaryWord(value, _: Anonymous) => value.fg("red")
    case LastToken(value) => value
    case Unknown(value) => value.black.onRed
    case Definition(value) => value.fg("light_yellow").bold
  }
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

class SyntaxParser(val input: ParserInput, words: Set[Word] = Set.empty) extends Parser {
  private val COMMENT_BASE = CharPredicate.Printable -- ")"
  private val STRING_BASE = CharPredicate.Printable -- "\""
  private val NO_SPACE = CharPredicate.Printable -- " "

  private val dictionary = words.map(word => (word.toLowerCase, SyntaxTokens.DictionaryWord(word, Constant(word, 45)))).toMap

  def InputLine: Rule1[Seq[SyntaxToken]] = rule {
    zeroOrMore(Whitespace | Comment | StringLiteral | NumberLiteral | Definition | DictionaryWord | LastToken | Unknown) ~ EOI
  }

  private def NewLine = rule {
    optional('\r') ~ '\n'
  }

  private def Whitespace = rule {
    capture(oneOrMore(ch(' ') | ch('\t') | NewLine)) ~> SyntaxTokens.Whitespace
  }

  private def Comment = rule {
    capture(
      optional('.') ~ '(' ~ EOI |
        optional('.') ~ "( " ~ zeroOrMore(COMMENT_BASE) ~ ")" |
        optional('.') ~ "( " ~ zeroOrMore(COMMENT_BASE) ~ EOI |
        "\\" ~ (EOI | NewLine) |
        "\\ " ~ zeroOrMore(CharPredicate.Printable) ~ (EOI | NewLine)
    ) ~> SyntaxTokens.Comment
  }

  private def StringLiteral = rule {
    capture(
      optional('.') ~ '"' ~ EOI |
        optional('.') ~ "\" " ~ zeroOrMore(STRING_BASE) ~ "\"" |
        optional('.') ~ "\" " ~ zeroOrMore(STRING_BASE) ~ EOI
    ) ~> SyntaxTokens.StringLiteral
  }

  private def NumberLiteral = rule {
    capture(oneOrMore(CharPredicate.Digit)) ~> SyntaxTokens.NumberLiteral
  }

  private def DictionaryWord = rule {
    valueMap(dictionary, ignoreCase = true)
  }

  private def LastToken = rule {
    capture(oneOrMore(NO_SPACE) ~ EOI) ~> SyntaxTokens.LastToken
  }

  private def Definition = rule {
    capture(":" ~ oneOrMore(ch(' ') | ch('\t') | NewLine) ~ oneOrMore(NO_SPACE)) ~> SyntaxTokens.Definition
  }

  private def Unknown = rule {
    capture(oneOrMore(NO_SPACE)) ~> SyntaxTokens.Unknown
  }
}
