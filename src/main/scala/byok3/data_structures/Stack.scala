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

import byok3.types.Stack
import cats.data.StateT
import cats.data.StateT._
import cats.implicits._

import scala.util.Try

object Stack {

  def push[A](value: A): StateT[Try, Stack[A], Unit] =
    modify(value :: _)

  def pop[A]: StateT[Try, Stack[A], A] =
    apply(stack => Try((stack.head, stack.tail).swap))

  def peek[A]: StateT[Try, Stack[A], A] =
    inspectF(stack => Try(stack.head))

  def arity1stackOp[A](f: A => A): StateT[Try, Stack[A], Unit] = for {
    a <- pop
    _ <- push(f(a))
  } yield ()

  def arity2stackOp[A](f: (A, A) => A): StateT[Try, Stack[A], Unit] = for {
    b <- pop
    a <- pop
    _ <- push(f(a, b))
  } yield ()

  def arity2stackOp2[A](f1: (A, A) => A)(f2: (A, A) => A): StateT[Try, Stack[A], Unit] = for {
    b <- pop
    a <- pop
    _ <- push(f1(a, b))
    _ <- push(f2(a, b))
  } yield ()

  def arity3stackOp[A](f: (A, A, A) => A): StateT[Try, Stack[A], Unit] = for {
    c <- pop
    b <- pop
    a <- pop
    _ <- push(f(a, b, c))
  } yield ()

  def arity3stackOp2[A](f1: (A, A, A) => A)(f2: (A, A, A) => A): StateT[Try, Stack[A], Unit] = for {
    c <- pop
    b <- pop
    a <- pop
    _ <- push(f1(a, b, c))
    _ <- push(f2(a, b, c))
  } yield ()
}
