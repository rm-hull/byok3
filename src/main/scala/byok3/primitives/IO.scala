package byok3.primitives

import java.time.LocalDate

import byok3.annonation.{Documentation, StackEffect}
import byok3.data_structures.Context
import byok3.data_structures.Context._
import byok3.data_structures.Memory._
import byok3.data_structures.Stack._
import byok3.types.Stack
import cats.data.StateT.get
import cats.implicits._

import scala.util.Try

object IO {

  @Documentation("convert signed number n to string of digits, and output.")
  @StackEffect("( n -- )")
  val `.` = for {
    a <- dataStack(pop)
    _ <- output(print(s"$a "))
  } yield ()

  @Documentation("display stack contents.")
  @StackEffect("( -- )")
  val `.S` = for {
    stack <- dataStack(get[Try, Stack[Int]])
    _ <- output(print(stack.reverse.mkString(" ") + " "))
  } yield ()

  @Documentation("outputs ascii as character.")
  @StackEffect("( ascii -- )")
  val EMIT = for {
    ascii <- dataStack(pop)
    _ <- output(print(ascii.toChar))
  } yield ()


  @Documentation("outputs u space characters.")
  @StackEffect("( u -- )")
  val SPACES = for {
    n <- dataStack(pop)
    _ <- output(print(' ' * n))
  } yield ()

  @Documentation("outputs the contents of addr for n bytes.")
  @StackEffect("( addr n -- )")
  val TYPE = for {
    addr <- dataStack(pop)
    n <- dataStack(pop)
    data <- memory(fetch(addr, n))
    _ <- output(print(data))
  } yield ()

  @Documentation("displays the MIT license text.")
  @StackEffect("( -- )")
  val LICENSE = output {
    val now = LocalDate.now
    println(
      s"""|The MIT License (MIT)
          |
          |Copyright (c) ${now.getYear} Richard Hull
          |
          |Permission is hereby granted, free of charge, to any person obtaining a copy of
          |this software and associated documentation files (the "Software"), to deal in
          |the Software without restriction, including without limitation the rights to
          |use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
          |the Software, and to permit persons to whom the Software is furnished to do so,
          |subject to the following conditions:
          |
          |The above copyright notice and this permission notice shall be included in all
          |copies or substantial portions of the Software.
          |
          |THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
          |IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
          |FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
          |COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
          |IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
          |CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
       """.stripMargin)
  }

  @Documentation("List the definition names in alphabetical order.")
  @StackEffect("( -- )")
  val WORDS = for {
    ctx <- get[Try, Context]
    _ <- output {
      println(ctx.dictionary.keys.toList.sorted.mkString(" "))
    }
  } yield ()
}