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

import byok3.Themes
import byok3.data_structures._
import cats.FlatMap
import org.jline.reader.{Highlighter, LineReader}
import org.jline.utils.{AttributedString, AttributedStringBuilder}

import scala.util.Try

class SyntaxHighlighter(implicit F: FlatMap[Try]) extends Highlighter {
  private var ctx: Option[Context] = None

  def setContext(ctx: Context): Unit = {
    this.ctx = Some(ctx)
  }

  override def setErrorPattern(errorPattern: java.util.regex.Pattern): Unit = {}

  override def setErrorIndex(errorIndex: Int): Unit = {}

  override def highlight(lineReader: LineReader, buffer: String): AttributedString = {
    val sb = new AttributedStringBuilder
    sb.ansiAppend(ctx.flatMap(c => Themes.Darkula(buffer, c).toOption).getOrElse(buffer))
    sb.toAttributedString
  }
}