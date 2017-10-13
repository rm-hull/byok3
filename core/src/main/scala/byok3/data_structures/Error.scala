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

import java.io.FileNotFoundException

case class Position(file: String, line: Int)
case class Error(errno: Int, message: String, pos: Option[Position])
  extends RuntimeException(pos.fold(message.trim) { p =>
    s"${message.trim}, in file: ${p.file}:${p.line}" })

case object Error {

  private val sysErrors = Map(
    0 -> "undefined",
    -1 -> "general abort",
    -2 -> "for abort\"",
    -3 -> "stack overflow",
    -4 -> "stack underflow",
    -5 -> "return stack overflow",
    -6 -> "return stack underflow",
    -7 -> "do loops nested too deeply",
    -8 -> "dictionary overflow",
    -9 -> "invalid memory address",
    -10 -> "division by zero",
    -11 -> "result out of range",
    -12 -> "argument type mismatch",
    -13 -> "word not found",
    -14 -> "use only during compilation",
    -15 -> "invalid forget",
    -16 -> "attempt to use zero-length string as name",
    -17 -> "pictured numeric ouput string overflow",
    -18 -> "pictured numeric ouput string overflow",
    -19 -> "word name too long",
    -20 -> "write to a read-only location",
    -21 -> "unsupported operation",
    -22 -> "unstructured",
    -23 -> "address alignment exception",
    -24 -> "invalid numeric argument",
    -25 -> "return stack imbalance",
    -26 -> "loop parameters unavailable",
    -27 -> "invalid recursion",
    -28 -> "user interrupt",
    -29 -> "compiler nesting",
    -30 -> "obsolescent feature",
    -31 -> ">BODY used on non-CREATEd definition",
    -32 -> "invalid name argument",
    -33 -> "Block read exception",
    -34 -> "Block write exception",
    -35 -> "Invalid block number",
    -36 -> "Invalid file position",
    -37 -> "File I/O exception",
    -38 -> "File not found")

  def apply(errno: Int): Error =
    apply(errno, "")

  def apply(errno: Int, additionalInfo: String): Error = {
    val msg = if (errno > 0) "user defined error" else sysErrors.getOrElse(errno, "undefined")
    new Error(errno, if (additionalInfo.trim.isEmpty) msg else s"$msg: $additionalInfo", None)
  }

  def apply(ex: Throwable, position: Option[Position] = None): Error = {
    val err = ex match {
      case err: Error => err
      case _: NotImplementedError => Error(-21)
      case _: UnsupportedOperationException => Error(-21)
      case _: FileNotFoundException => Error(-38)
      case _: NoSuchElementException => Error(-4)
      case _: IndexOutOfBoundsException => Error(-9, ex.getMessage)
      case ar: ArithmeticException if ar.getMessage == "/ by zero" => Error(-10)
      case _ => Error(0, s"[${ex.getClass.getName}] ${ex.getMessage}")
    }
    err.copy(pos = position)
  }
}

