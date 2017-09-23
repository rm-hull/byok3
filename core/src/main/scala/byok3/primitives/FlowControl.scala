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

package byok3.primitives

import byok3.annonation.{Documentation, Internal}
import byok3.data_structures.Context._
import byok3.data_structures.CoreMemory._
import byok3.data_structures.Dictionary._
import byok3.data_structures.Stack.{pop, push, peek => speek}
import byok3.data_structures.{Error, IP, W}
import byok3.primitives.StackManipulation.RDROP
import byok3.types.AppState
import byok3.helpers._
import cats.data.StateT._
import cats.implicits._

import scala.util.Failure

object FlowControl {

  private def `ip++` = for {
    ip <- IP()
    _ <- IP(inc(ip))
  } yield ()

  @Internal
  val __NEST = for {
    addr <- IP()
    next <- W()
    _ <- IP(inc(next))
    _ <- returnStack(push(addr))
  } yield ()

  @Documentation(
    """
      | Return control to the calling definition specified by nest-sys. Before
      | executing EXIT within a do-loop, a program shall discard the loop-control
      | parameters by executing UNLOOP.
    """, stackEffect = "Execution: ( -- ) ( R: nest-sys -- )")
  val EXIT = for {
    _ <- returnStackNotEmpty
    addr <- returnStack(pop)
    _ <- IP(addr)
  } yield ()

  @Documentation("TODO", stackEffect = "( i*x -- )")
  val THROW: AppState[Unit] =
    dataStack(pop).flatMapF[Unit](err => Failure(Error(err)))

  val `?ERROR` = for {
    err <- dataStack(pop)
    cond <- dataStack(pop)
  } yield if (cond == 0) () else throw Error(err)

  @Documentation("Skip leading space delimiters. Parse name delimited by a space. Find name and return xt, the execution token for name", stackEffect = "( \"<spaces>name\" -- xt )")
  val `'` = for {
    token <- nextToken()
    name = token.value.toUpperCase
    _ <- guard(name.nonEmpty, Error(-16))
    xt <- dictionary(addressOf(name))
    _ <- dataStack(push(xt))
  } yield ()

  @Documentation("Remove xt from the stack and perform the semantics identified by it. Other stack effects are due to the word EXECUTEd", stackEffect = "( i*x xt -- j*x )")
  val EXECUTE = for {
    xt <- dataStack(pop)
    instr <- dictionary(instruction(xt))
    _ <- exec(instr.name)
  } yield ()

  @Documentation("", stackEffect = "( -- )")
  val BRANCH = for {
    ip <- IP()
    jmp <- memory(peek(ip))
    _ <- IP(ip + jmp)
  } yield ()

  @Documentation("", stackEffect = "( x -- )")
  val `0BRANCH` = for {
    x <- dataStack(pop)
    _ <- if (x == 0) BRANCH else `ip++`
  } yield ()

  val `(DO)` = for {
    index <- dataStack(pop)
    limit <- dataStack(pop)
    _ <- returnStack(push(limit))
    _ <- returnStack(push(index))
  } yield ()

  val `(?DO)` = for {
    index <- dataStack(pop)
    limit <- dataStack(pop)
    _ <- if (index == limit) BRANCH
         else returnStack(sequence(push(limit), push(index)))
  } yield ()


  @Documentation("n | u is a copy of the current (innermost) loop index. An ambiguous condition exists if the loop control parameters are unavailable", stackEffect = "( -- n | u ) ( R: loop-sys -- loop-sys )")
  val I = for {
    n <- returnStack(speek)
    _ <- dataStack(push(n))
  } yield ()

  @Documentation("n | u is a copy of the next-outer loop index. An ambiguous condition exists if the loop control parameters of the next-outer loop, loop-sys1, are unavailable", stackEffect = "( -- n | u ) ( R: loop-sys1 loop-sys2 -- loop-sys1 loop-sys2 )")
  val J = for {
    n <- returnStack(inspect(_(2)))
    _ <- dataStack(push(n))
  } yield ()

  val `(LEAVE)` = for {
    _ <- returnStack(pop)
    limit <- returnStack(speek)
    index = limit - 1
    _ <- returnStack(push(index))
  } yield ()

  val `(LOOP)` = for {
    _ <- dataStack(push(1))
    _ <- `(+LOOP)`
  } yield ()

  val `(+LOOP)` = for {
    step <- dataStack(pop)
    index <- returnStack(pop)
    limit <- returnStack(speek)
    _ <- returnStack(push(index + step))
    _ <- if (index + step != limit) BRANCH
         else sequence(`ip++`, RDROP, RDROP)
  } yield ()
}
