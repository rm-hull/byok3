package byok3

import cats.data.State
import cats.data.State._

object Sequence {

  def apply[S, A](sas: State[S, A]*): State[S, List[A]] =
    sas.foldRight(pure[S, List[A]](List.empty)) {
      (f, acc) => f.map2(acc)(_ :: _)
    }
}

object Stack {

  type Stack = List[Int]

  val empty: Stack = List.empty

  def push(value: Int): State[Stack, Unit] = modify(value :: _)

  val pop: State[Stack, Int] = State {
    case Nil => sys.error("Empty stack")
    case h :: t => (t, h)
  }

  val peek: State[Stack, Int] = for {
    a <- pop
    _ <- push(a)
  } yield a

  def arity1stackOp(f: Int => Int): State[Stack, Unit] = for {
    a <- pop
    _ <- push(f(a))
  } yield ()

  def arity2stackOp(f: (Int, Int) => Int): State[Stack, Unit] = for {
    a <- pop
    b <- pop
    _ <- push(f(a, b))
  } yield ()

  def arity2stackOp2(f1: (Int, Int) => Int)(f2: (Int, Int) => Int): State[Stack, Unit] = for {
    a <- pop
    b <- pop
    _ <- push(f1(a, b))
    _ <- push(f2(a, b))
  } yield ()
}
