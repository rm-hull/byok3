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
import org.parboiled2._
import shapeless.HNil


object SyntaxTokens {

  sealed trait SyntaxToken {
    val value: String
  }

  case class Whitespace(value: String) extends SyntaxToken
  case class Comment(value: String) extends SyntaxToken
  case class StringLiteral(value: String) extends SyntaxToken
}

class SyntaxParser(val input: ParserInput) extends Parser {
  private val COMMENT_BASE = CharPredicate.Printable -- ")"
  private val STRING_BASE = CharPredicate.Printable -- "\""

  def InputLine: Rule1[Seq[SyntaxToken]] = rule {
    zeroOrMore(Whitespace | Comment | StringLiteral) ~ EOI
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
}
