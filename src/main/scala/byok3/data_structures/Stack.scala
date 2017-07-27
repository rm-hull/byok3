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
