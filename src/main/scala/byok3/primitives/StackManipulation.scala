package byok3.primitives

import byok3.annonation.Documentation
import byok3.data_structures.Context._
import byok3.data_structures.Stack._
import byok3.types.Stack
import cats.data.StateT._
import cats.implicits._

import scala.util.Try

object StackManipulation {

  @Documentation("the number of single-cell values contained in the data stack before n was placed on the stack", stackEffect = "( -- n )")
  val DEPTH = dataStack {
    for {
      stack <- get[Try, Stack[Int]]
      n = stack.length
      _ <- push(n)
    } yield ()
  }

  @Documentation("drop top stack element", stackEffect = "( x -- )")
  val DROP = dataStack {
    pop.map(_ => ())
  }

  @Documentation("drop cell pair x1 x2 from the stack", stackEffect = "( x1 x2 -- )")
  val `2DROP` = dataStack {
    for {
      _ <- pop
      _ <- pop
    } yield ()
  }

  @Documentation("copy NOS (next of stack) to top of stack", stackEffect = "( x1 x2 -- x1 x2 x1)")
  val OVER = dataStack {
    for {
      x2 <- pop
      x1 <- pop
      _ <- push(x1)
      _ <- push(x2)
      _ <- push(x1)
    } yield ()
  }

  @Documentation("remove NOS", stackEffect = "( x1 x2 -- x2 )")
  val NIP = dataStack {
    for {
      x2 <- pop
      _ <- pop
      _ <- push(x2)
    } yield ()
  }

  @Documentation("copy the first (top) stack item below the second stack item", stackEffect = "( x1 x2 -- x2 x1 x2 )")
  val TUCK = dataStack {
    for {
      x2 <- pop
      x1 <- pop
      _ <- push(x2)
      _ <- push(x1)
      _ <- push(x2)
    } yield ()
  }

  @Documentation("rotate the top three stack entries", stackEffect = "( x1 x2 x3 -- x2 x3 x1 )")
  val ROT = dataStack {
    for {
      x3 <- pop
      x2 <- pop
      x1 <- pop
      _ <- push(x2)
      _ <- push(x3)
      _ <- push(x1)
    } yield ()
  }

  @Documentation("rotate the top three stack entries", stackEffect = "( x1 x2 x3 -- x3 x1 x2 )")
  val `-ROT` = dataStack {
    for {
      x3 <- pop
      x2 <- pop
      x1 <- pop
      _ <- push(x3)
      _ <- push(x1)
      _ <- push(x2)
    } yield ()
  }

  @Documentation("swap top two stack elements", stackEffect = "( x1 x2 -- x2 x1)")
  val SWAP = dataStack {
    for {
      x2 <- pop
      x1 <- pop
      _ <- push(x2)
      _ <- push(x1)
    } yield ()
  }

  @Documentation("duplicate top stack element", stackEffect = "( x -- x x )")
  val DUP = dataStack {
    for {
      x <- peek
      _ <- push(x)
    } yield ()
  }

  @Documentation("duplicate top stack element if it is non-zero", stackEffect = "( x -- 0 | x x )")
  val `?DUP` = dataStack {
    peek.flatMap(x => if (x == 0) pure(()) else push(x))
  }

  @Documentation("the number of single-cell values contained in the return stack", stackEffect = "( -- n )")
  val RDEPTH = for {
    stack <- returnStack(get[Try, Stack[Int]])
    n = stack.length
    _ <- dataStack(push(n))
  } yield ()

  @Documentation("move x to the return stack", stackEffect = "( x -- )  ( R:  -- x)")
  val `>R` = for {
    x <- dataStack(pop)
    _ <- returnStack(push(x))
  } yield ()

  @Documentation("move x from the return stack to the data stack", stackEffect = "( -- x ) ( R:  x -- )")
  val `R>` = for {
    x <- returnStack(pop)
    _ <- dataStack(push(x))
  } yield ()

  @Documentation("copy x from the return stack to the data stack", stackEffect = "( -- x ) ( R:  x -- x)")
  val `R@`= for {
    x <- returnStack(peek)
    _ <- dataStack(push(x))
  } yield ()
}