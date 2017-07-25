package byok3.data_structures

import byok3.types.Stack
import cats.data.State
import cats.data.State._

object Stack {

  private val stackUnderflowError = new NoSuchElementException("stack underflow") // MError(-4)

  def empty[A]: Stack[A] = List.empty[A]

  def push[A](value: A): State[Stack[A], Unit] = modify(value :: _)

  def pop[A]: State[Stack[A], A] = State {
    case Nil => throw stackUnderflowError
    case h :: t => (t, h)
  }

  def peek[A]: State[Stack[A], A] =
    inspect(_.headOption.getOrElse(throw stackUnderflowError))

  def arity1stackOp[A](f: A => A): State[Stack[A], Unit] = for {
    a <- pop
    _ <- push(f(a))
  } yield ()

  def arity2stackOp[A](f: (A, A) => A): State[Stack[A], Unit] = for {
    b <- pop
    a <- pop
    _ <- push(f(a, b))
  } yield ()

  def arity2stackOp2[A](f1: (A, A) => A)(f2: (A, A) => A): State[Stack[A], Unit] = for {
    b <- pop
    a <- pop
    _ <- push(f1(a, b))
    _ <- push(f2(a, b))
  } yield ()

  def arity3stackOp[A](f: (A, A, A) => A): State[Stack[A], Unit] = for {
    c <- pop
    b <- pop
    a <- pop
    _ <- push(f(a, b, c))
  } yield ()

  def arity3stackOp2[A](f1: (A, A, A) => A)(f2: (A, A, A) => A): State[Stack[A], Unit] = for {
    c <- pop
    b <- pop
    a <- pop
    _ <- push(f1(a, b, c))
    _ <- push(f2(a, b, c))
  } yield ()
}
