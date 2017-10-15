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
import byok3.data_structures.CoreMemory
import byok3.data_structures.Stack._
import cats.implicits._

object Arithmetics {

  @Documentation("adds x1 and x2, leaves result x3", stackEffect = "( x1 x2 -- x3 )")
  val + = dataStack(arity2stackOp(_ + _))

  @Documentation("subtracts x2 from x1, leaves result x3", stackEffect = "( x1 x2 -- x3 )")
  val - = dataStack(arity2stackOp(_ - _))

  @Documentation("multiplies x1 with x2, leaves result x3", stackEffect = "( x1 x2 -- x3 )")
  val * = dataStack(arity2stackOp(_ * _))

  @Documentation("divides x1 by x2, leaves result x3", stackEffect = "( x1 x2 -- x3 )")
  val / = dataStack(arity2stackOp(_ / _))

  @Documentation("increments x1 by 1", stackEffect = "( x1 -- x2 )")
  val `1+` = dataStack(arity1stackOp(_ + 1))

  @Documentation("decrements x1 by 1", stackEffect = "( x1 -- x2 )")
  val `1-` = dataStack(arity1stackOp(_ - 1))

  @Documentation("increments x1 by 2", stackEffect = "( x1 -- x2 )")
  val `2+` = dataStack(arity1stackOp(_ + 2))

  @Documentation("decrements x1 by 2", stackEffect = "( x1 -- x2 )")
  val `2-` = dataStack(arity1stackOp(_ - 2))

  @Documentation("multiply x1 by 2", stackEffect = "( x1 -- x2 )")
  val `2*` = dataStack(arity1stackOp(_ * 2))

  @Documentation("divide n1 by 2", stackEffect = "( n1 -- n2 )")
  val `2/` = dataStack(arity1stackOp(_ / 2))

  @Documentation("return absolute value of n", stackEffect = "( n -- u )")
  val ABS = dataStack(arity1stackOp(math.abs))

  @Documentation("change sign of n1", stackEffect = "( n1 -- n2 )")
  val NEGATE = dataStack(arity1stackOp(-_))

  @Documentation("return the lesser of the two signed numbers n1 and n2", stackEffect = "( n1 n2 -- n3 )")
  val MIN = dataStack(arity2stackOp(_ min _))

  @Documentation("return the greater of the two signed numbers n1 and n2", stackEffect = "( n1 n2 -- n3 )")
  val MAX = dataStack(arity2stackOp(_ max _))

  @Documentation("calculates and returns remainder of division n1/n2", stackEffect = "( n1 n2 -- n3 )")
  val `MOD` = dataStack(arity2stackOp(_ % _))

  @Documentation("calculates and returns remainder and quotient of division n1/n2", stackEffect = "( n1 n2 -- n-rem n-quot )")
  val `/MOD` = dataStack(arity2stackOp2[Int](_ % _)(_ / _))

  @Documentation("multiplies then divides (n1 x n2) / n3", stackEffect = "( n1 n2 n3 -- n4 )")
  val `*/` = dataStack(arity3stackOp[Int](_ * _ / _))

  @Documentation("multiplies then divides (n1 x n2) / n3, returning the remainder in n4 and quotient in n5", stackEffect = "( n1 n2 n3 -- n4 n5)")
  val `*/MOD` = dataStack(arity3stackOp2[Int](_ * _ / _)(_ * _ / _))

}

object DoublePrecisionNumbers {

  val NBITS = 8 * CoreMemory.CELL_SIZE

  def sgn(n: Int) = if (n < 0) -1 else 0

  @Documentation("multiplies x1 with x2, leaves 64-bit result split in x3 and x4", stackEffect = "( x1 x2 -- x3 x4)")
  val `M*` = dataStack {
    for {
      x2 <- pop[Int]
      x1 <- pop[Int]
      result: Long = x1 * x2
      _ <- push((result & 0xFFFFFFFF).toInt)
      _ <- push((result >> 32).toInt)
    } yield ()
  }

  val `UM*` = `M*`

  @Documentation("Double-length addition", stackEffect = "( al ah bl bh -- sl sh )")
  val `D+` = dataStack {
    for {
      bh <- pop[Int]
      bl <- pop[Int]
      ah <- pop[Int]
      al <- pop[Int]
      sl = al + bl
      sh = ah + bh + (if (al < bl) 1 /* carry */ else 0)
      _ <- push(sl)
      _ <- push(sh)
    } yield ()
  }

  @Documentation("Double-length subtraction", stackEffect = "( al ah bl bh -- sl sh )")
  val `D-` = dataStack {
    for {
      bh <- pop[Int]
      bl <- pop[Int]
      ah <- pop[Int]
      al <- pop[Int]
      sl = al - bl
      sh = ah - bh - (if (al < bl) 1 /* borrow */ else 0)
      _ <- push(sl)
      _ <- push(sh)
    } yield ()
  }

  private def DULT(du1l: Int, du1h: Int, du2l: Int, du2h: Int) =
    if (du2h < du1h) false
    else if (du2h == du1h) du1l < du2l
    else true

  @Documentation("Divide ud by u1, giving the quotient u3 and the remainder u2. All values and arithmetic are unsigned. An ambiguous condition exists if u1 is zero or if the quotient lies outside the range of a single-cell unsigned integer", stackEffect = "( ud u1 -- u2 u3 )")
  val `UM/MOD` = dataStack {
    for {
      bh <- pop[Int]
      ah <- pop[Int]
      al <- pop[Int]
      (rem, quot) = UMSMOD(al, ah, bh)
      _ <- push(unsigned(rem))
      _ <- push(unsigned(quot))
    } yield ()
  }

  private def UMSMOD(al$: Int, ah$: Int, bh$: Int) = {
    var al = al$
    var ah = ah$
    var bl = 0
    var bh = bh$
    var sh = 0
    var sl = 0
    var quot = 0

    for (_ <- 0 to NBITS) {

      if (!DULT(al, ah, bl, bh)) {
        sl = al - bl
        sh = ah - bh - (if (al < bl) 1 else 0)
        ah = sh
        al = sl
        quot |= 1
      }

      quot = quot << 1
      bl = (bl >> 1) | (bh << (NBITS - 1))
      bh = bh >> 1
    }

    if (!DULT(al, ah, bl, bh)) {
      al -= bl
      quot |= 1
    }

    (al, quot) // al --> remainder
  }


  val `MU/MOD` = dataStack {
    for {
      bdiv <- pop[Int]
      am <- pop[Int]
      al <- pop[Int]
      (rem, quotl, quoth) = MUSMOD(al, am, bdiv)
      _ <- push(rem)
      _ <- push(quotl)
      _ <- push(quoth)
    } yield ()
  }

  private def MUSMOD(al$: Int, am$: Int, bdiv: Int) = {
    var al = al$
    var am = am$
    var ah = 0
    var ql = 0
    var qh = 0

    for (_ <- 0 to 2 * NBITS) {
      if (bdiv <= ah) {
        ah = ah - bdiv
        ql |= 1
      }
      qh = (qh << 1) | (ql >> (NBITS - 1))
      ql = ql << 1
      ah = (ah << 1) | (am >> (NBITS - 1))
      am = (am << 1) | (al >> (NBITS - 1))
      al = al << 1
    }

    if (bdiv <= ah) {
      ah -= bdiv
      ql |= 1
    }

    (ah, ql, qh)  // ah --> remainder
  }
}
