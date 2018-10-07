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

package byok3.console

import byok3.data_structures.Context.deref
import byok3.data_structures._
import byok3.implicits._
import cats.FlatMap
import org.jline.reader.{Highlighter, LineReader}
import org.jline.utils.{AttributedString, AttributedStringBuilder}

import scala.util.Try

class SyntaxHighlighter(implicit F: FlatMap[Try]) extends Highlighter {
  private var ctx: Option[Context] = None

  def setContext(ctx: Context): Unit = {
    this.ctx = Some(ctx)
  }

  override def highlight(lineReader: LineReader, buffer: String): AttributedString = {
    val sb = new AttributedStringBuilder

    for ((token, index, trailingSpaces) <- tokenize(buffer)) {
      sb.ansiAppend(colorize(token, buffer, index))
      sb.append(trailingSpaces)
    }
    sb.toAttributedString
  }

  private[console] def tokenize(line: String): Iterable[(String, Int, String)] = {
    "(\\S+)(\\s*)".r.findAllIn(line).matchData.toIterable.map { m => (m.group(1), m.start, m.group(2)) }
  }

  private def colorize(token: String, line: String, index: Int): String = {

    if (isComment(token, line, index)) {
      return token.blue
    }

    if (isDefinition(token, line, index)) {
      return token.yellow.bold
    }

    if (isLiteral(token, line, index)) {
      return token.magenta
    }

    isDictionaryWord(token, line, index).map {
      case _: SystemDefined => token.fg(131).bold
      case _: Primitive => token.fg(131).bold // indian_red_1a
      case _: Constant => token.fg(119)       // light_green
      case _: Variable => token.cyan
      case _: UserDefined =>  token.white
      case _: Anonymous => token.red
    }.getOrElse(token)
  }

  private def isComment(token: String, line: String, index: Int): Boolean = {
    if (List("\\", "(", ".(", ")").contains(token)) {
      return true
    }

    // Backslash trumps parens
    if (line.substring(0, index).contains("\\")) {
      return true
    }

    // Text inside comment?
    for (ch <- line.substring(0, index).reverseIterator) {
      if (ch == ')') return false
      if (ch == '(') return true
    }

    false
  }

  private def isDefinition(token: String, line: String, index: Int): Boolean =
    token == ":" || (index >= 2 && line.substring(index - 2, index) == ": ")

  private def isLiteral(token: String, line: String, index: Int): Boolean = {
    val base = ctx.flatMap(deref("BASE").runA(_).toOption).getOrElse(10)

    // check if is a number (in current base) or if in quotes
    // 'in quotes' == odd-number of dbl-quotes before index

    if (token.toNumber(base).orElse(token.fromChar).isSuccess) {
      return true
    }

    if (List("\"", ".\"").contains(token)) {
      return true
    }

    line.substring(0, index).count(_ == '"') % 2 == 1
  }

  private def isDictionaryWord(token: String, line: String, index: Int): Option[ExecutionToken] =
    ctx.flatMap(_.dictionary.get(token.toUpperCase))
}