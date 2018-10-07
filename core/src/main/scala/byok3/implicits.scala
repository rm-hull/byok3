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

import byok3.data_structures.Error

import scala.util.{Failure, Success, Try}

package object implicits {

  implicit class OptionOps[A](opt: Option[A]) {
    def toTry(ex: Exception): Try[A] =
      opt.map(Success(_)).getOrElse(Failure(ex))
  }

  implicit class ListOps[A](l: List[A]) {
    def remove[A](i: Int) = {
      val (head, _ :: tail) = l.splitAt(i)
      head ::: tail
    }
  }

  implicit class StringOps(s: String) {

    private val quotedChar = "\'(.)\'".r
    private val radixPrefix = Map('#' -> 10, '$' -> 16, '%' -> 2)

    private def parsePrefix(value: String) = {
      val radix = radixPrefix.get(value.charAt(0))
      radix.toTry(Error(-13, value))
        .flatMap(r => Try(Integer.parseInt(value.substring(1), r)))
    }

    def toNumber(radix: Int) =
      Try(Integer.parseInt(s, radix)).orElse(parsePrefix(s))

    def fromChar = s match {
      case quotedChar(ch) => Success(ch.codePointAt(0))
      case _ => Failure(new NumberFormatException(s"For input string: $s"))
    }
  }

  implicit class RainbowString(s: String) {

    import Console._

    def black = BLACK + s + RESET
    def red = RED + s + RESET
    def green = GREEN + s + RESET
    def yellow = YELLOW + s + RESET
    def blue = BLUE + s + RESET
    def magenta = MAGENTA + s + RESET
    def cyan = CYAN + s + RESET
    def white = WHITE + s + RESET
    def onBlack = BLACK_B + s + RESET
    def onRed = RED_B + s + RESET
    def onGreen = GREEN_B + s + RESET
    def onYellow = YELLOW_B + s + RESET
    def onBlue = BLUE_B + s + RESET
    def onMagenta = MAGENTA_B + s + RESET
    def onCyan = CYAN_B + s + RESET
    def onWhite = WHITE_B + s + RESET
    def bold = BOLD + s + RESET
    def underlined = UNDERLINED + s + RESET
    def blink = BLINK + s + RESET
    def reversed = REVERSED + s + RESET
    def invisible = INVISIBLE + s + RESET

    /** ANSI256 */
    def fg(i: Int)= s"\u001b[38;5;${i.abs % 256}m" + s + RESET
    def bg(i: Int)= s"\u001b[48;5;${i.abs % 256}m" + s + RESET
  }

}