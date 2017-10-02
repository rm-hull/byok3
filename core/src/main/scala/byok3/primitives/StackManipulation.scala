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

import byok3.annonation.Documentation
import byok3.data_structures.Context._
import byok3.data_structures.Error
import byok3.data_structures.Stack._
import byok3.implicits._
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

  @Documentation("copy cell pair x1 x2 to the top of the stack", stackEffect = "( x1 x2 x3 x4 -- x1 x2 x3 x4 x1 x2)")
  val `2OVER` = dataStack {
    for {
      x4 <- pop
      x3 <- pop
      x2 <- pop
      x1 <- pop
      _ <- push(x1)
      _ <- push(x2)
      _ <- push(x3)
      _ <- push(x4)
      _ <- push(x1)
      _ <- push(x2)
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

  @Documentation("exchange the top two cell pairs", stackEffect = "( x1 x2 x3 x4 -- x3 x4 x1 x2)")
  val `2SWAP` = dataStack {
    for {
      x4 <- pop
      x3 <- pop
      x2 <- pop
      x1 <- pop
      _ <- push(x3)
      _ <- push(x4)
      _ <- push(x1)
      _ <- push(x2)
    } yield ()
  }

  @Documentation("duplicate top stack element", stackEffect = "( x -- x x )")
  val DUP = dataStack {
    for {
      x <- peek
      _ <- push(x)
    } yield ()
  }

  @Documentation("duplicate cell pair x1 x2", stackEffect = "( x1 x2 -- x1 x2 x1 x2 )")
  val `2DUP` = dataStack {
    for {
      x2 <- pop
      x1 <- pop
      _ <- push(x1)
      _ <- push(x2)
      _ <- push(x1)
      _ <- push(x2)
    } yield ()
  }

  @Documentation("duplicate top stack element if it is non-zero", stackEffect = "( x -- 0 | x x )")
  val `?DUP` = dataStack {
    peek.flatMap(x => if (x == 0) pure(()) else push(x))
  }

  @Documentation("remove u. Copy the xu to the top of the stack", stackEffect = "( xu ... x1 x0 u -- xu ... x1 x0 xu )")
  val PICK = dataStack {
    for {
      u <- pop
      stack <- get[Try, Stack[Int]]
      xu = Try(stack(u)).getOrElse(throw Error(-11)) // result out of range
      _ <- push(xu)
    } yield ()
  }

  @Documentation("Remove u. Rotate u+1 items on the top of the stack. An ambiguous condition exists if there are less than u+2 items on the stack before ROLL is executed", stackEffect = "( xu xu-1 ... x0 u -- xu-1 ... x0 xu )")
  val ROLL = dataStack {
    for {
      u <- pop
      stack <- get[Try, Stack[Int]]
      xu = Try(stack(u)).getOrElse(throw Error(-11)) // result out of range
      _ <- set(stack.remove(u))
      _ <- push(xu)
    } yield ()
  }

  @Documentation("the number of single-cell values contained in the return stack", stackEffect = "( -- n )")
  val RDEPTH = for {
    stack <- returnStack(get[Try, Stack[Int]])
    n = stack.length
    _ <- dataStack(push(n))
  } yield ()

  @Documentation("drop top return stack element", stackEffect = "( -- ) ( R:  x -- )")
  val RDROP = for {
    _ <- returnStackNotEmpty
    _ <- returnStack(pop)
  } yield ()

  @Documentation("move x to the return stack", stackEffect = "( x -- )  ( R:  -- x)")
  val `>R` = for {
    x <- dataStack(pop)
    _ <- returnStack(push(x))
  } yield ()

  @Documentation("move x from the return stack to the data stack", stackEffect = "( -- x ) ( R:  x -- )")
  val `R>` = for {
    _ <- returnStackNotEmpty
    x <- returnStack(pop)
    _ <- dataStack(push(x))
  } yield ()

  @Documentation("copy x from the return stack to the data stack", stackEffect = "( -- x ) ( R:  x -- x)")
  val `R@` = for {
    _ <- returnStackNotEmpty
    x <- returnStack(peek)
    _ <- dataStack(push(x))
  } yield ()
}