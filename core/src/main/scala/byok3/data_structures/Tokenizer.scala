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

import byok3.types.Word

import scala.annotation.tailrec

sealed trait Tokenizer {
  val value: Word
  val offset: Int
  val exhausted: Boolean
  def next(delim: String): Tokenizer
}

object Tokenizer {
  val delimiters = "[ \t\n]"

  def apply(in: String, delim: String = delimiters): Tokenizer =
    Token("", -1, Option(in).getOrElse("")).next(delim)
}

case object EndOfData extends Tokenizer {
  override val value = ""
  override val offset = 0
  override val exhausted = true
  override def next(delim: String) = EndOfData
}

case class Token(value: String, offset: Int, in: String) extends Tokenizer {
  @tailrec
  override final def next(delim: String) = {
    val nextOffset = value.length + offset + 1
    if (nextOffset >= in.length) EndOfData
    else in.substring(nextOffset).split(delim).headOption match {
      case None => EndOfData
      case Some(t) if t.isEmpty => Token(t, nextOffset, in).next(delim)
      case Some(t) => Token(t, nextOffset, in)
    }
  }

  @volatile override lazy val exhausted = in.endsWith(value) && in.length - offset == value.length
}


