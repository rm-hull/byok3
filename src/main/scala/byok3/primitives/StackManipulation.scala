package byok3.primitives

import byok3.annonation.{Documentation, StackEffect}
import byok3.data_structures.Context._
import byok3.data_structures.Stack._
import byok3.types.Stack
import cats.data.StateT._
import cats.implicits._

import scala.util.Try

object StackManipulation {

  @Documentation("the number of single-cell values contained in the data stack before n was placed on the stack.")
  @StackEffect("( -- n )")
  val DEPTH = dataStack {
    for {
      stack <- get[Try, Stack[Int]]
      _ <- push(stack.length)
    } yield ()
  }

  @Documentation("drop top stack element.")
  @StackEffect("( x -- )")
  val DROP = dataStack {
    pop.map(_ => ())
  }

  @Documentation("copy NOS (next of stack) to top of stack.")
  @StackEffect("( x1 x2 -- x1 x2 x1)")
  val OVER = dataStack {
    for {
      a <- pop
      b <- pop
      _ <- push(b)
      _ <- push(a)
      _ <- push(b)
    } yield ()
  }

  @Documentation("remove NOS.")
  @StackEffect("( x1 x2 -- x2 )")
  val NIP = dataStack {
    for {
      a <- pop
      b <- pop
      _ <- push(a)
    } yield ()
  }

  @Documentation("copy the first (top) stack item below the second stack item.")
  @StackEffect("( x1 x2 -- x2 x1 x2 )")
  val TUCK = dataStack {
    for {
      a <- pop
      b <- pop
      _ <- push(a)
      _ <- push(b)
      _ <- push(a)
    } yield ()
  }

  @Documentation("rotate the top three stack entries.")
  @StackEffect("( x1 x2 x3 -- x2 x3 x1 )")
  val ROT = dataStack {
    for {
      a <- pop
      b <- pop
      c <- pop
      _ <- push(b)
      _ <- push(a)
      _ <- push(c)
    } yield ()
  }

  @Documentation("rotate the top three stack entries.")
  @StackEffect("( x1 x2 x3 -- x3 x1 x2 )")
  val `-ROT` = dataStack {
    for {
      a <- pop
      b <- pop
      c <- pop
      _ <- push(a)
      _ <- push(c)
      _ <- push(b)
    } yield ()
  }

  @Documentation("swap top two stack elements.")
  @StackEffect("( x1 x2 -- x2 x1)")
  val SWAP = dataStack {
    for {
      a <- pop
      b <- pop
      _ <- push(a)
      _ <- push(b)
    } yield ()
  }

  @Documentation("duplicate top stack element.")
  @StackEffect("( x -- x x )")
  val DUP = dataStack {
    for {
      a <- peek
      _ <- push(a)
    } yield ()
  }

  @Documentation("duplicate top stack element if it is non-zero.")
  @StackEffect("( x -- 0 | x x )")
  val `?DUP` = dataStack {
    peek.flatMap(a => if (a == 0) pure(()) else push(a))
  }

  @Documentation("the number of single-cell values contained in the return stack.")
  @StackEffect("( -- n )")
  val RDEPTH = for {
    stack <- returnStack(get[Try, Stack[Int]])
    _ <- dataStack(push(stack.length))
  } yield ()

  @Documentation("move x to the return stack.")
  @StackEffect("( x -- )  ( R:  -- x)")
  val `>R` = for {
    a <- dataStack(pop)
    _ <- returnStack(push(a))
  } yield ()

  @Documentation("move x from the return stack to the data stack.")
  @StackEffect("( -- x ) ( R:  x -- )")
  val `R>` = for {
    a <- returnStack(pop)
    _ <- dataStack(push(a))
  } yield ()

  @Documentation("copy x from the return stack to the data stack.")
  @StackEffect("( -- x ) ( R:  x -- x)")
  val `R@`= for {
    a <- returnStack(peek)
    _ <- dataStack(push(a))
  } yield ()
}