package byok3.primitives

import byok3.data_structures.Context._
import byok3.data_structures.Stack._
import byok3.types.Stack
import cats.data.State._

object StackManip {

  val depth = dataStack {
    for {
      stack <- get[Stack[Int]]
      _ <- push(stack.length)
    } yield ()
  }

  val drop = dataStack {
    pop.map(_ => ())
  }

  val swap = dataStack {
    for {
      a <- pop
      b <- pop
      _ <- push(a)
      _ <- push(b)
    } yield ()
  }

  val dup = dataStack {
    for {
      a <- pop
      _ <- push(a)
      _ <- push(a)
    } yield ()
  }

  val `>R` = for {
    a <- dataStack(pop)
    _ <- returnStack(push(a))
  } yield ()

  val `R>` = for {
    a <- returnStack(pop)
    _ <- dataStack(push(a))
  } yield ()
}