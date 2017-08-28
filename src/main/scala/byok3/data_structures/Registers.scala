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

import byok3.data_structures.Context._
import byok3.data_structures.CoreMemory._
import byok3.data_structures.Dictionary._
import byok3.types.{AppState, Data, Word}
import cats.implicits._


sealed abstract class Register(name: Word) {

  private def set(data: Data): AppState[Unit] = for {
    xt <- dictionary(instruction(name))
    addr = xt.asInstanceOf[Constant].value
    _ <- memory(poke(addr, data))
  } yield ()

  private def get: AppState[Data] = for {
    xt <- dictionary(instruction(name))
    addr = xt.asInstanceOf[Constant].value
    data <- memory(peek(addr))
  } yield data

  def apply() = get

  def apply(data: Data) = set(data)
}

object DP extends Register("DP")
object IP extends Register("IP")
object W extends Register("W")
object XT extends Register("XT")
