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
import byok3.implicits._
import byok3.SyntaxTokens._

import scala.util.Try

object Themes {

  trait ColorTheme {
    def colorize(token: SyntaxToken): String

    def apply(text: String, ctx: Context): Try[String] =
      new SyntaxParser(text, ctx).InputLine.run().map {
        _.map(colorize).mkString
      }
  }

  object Darkula extends ColorTheme {
    override def colorize(token: SyntaxToken): String = token match {
      case Whitespace(value) => value
      case Comment(value) => value.fg("grey_35")
      case StringLiteral(value) => value.fg("green_4")
      case NumberLiteral(value) => value.fg("deep_sky_blue_2")
      case DictionaryWord(value, _:SystemDefined) => value.fg("dark_orange").bold
      case DictionaryWord(value, _:Primitive) => value.fg("dark_orange").bold
      case DictionaryWord(value, _:Constant) => value.fg("purple_3")
      case DictionaryWord(value, _:Variable) => value.fg("purple_3")
      case DictionaryWord(value, _:UserDefined) => value.fg("white")
      case DictionaryWord(value, _:Anonymous) => value.fg("red")
      case LastToken(value) => value
      case Unknown(value) => value.black.onRed
      case Definition(value) => value.fg("light_yellow").bold
    }
  }
}
