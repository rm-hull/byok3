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
import byok3.data_structures.Stack._

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

  @Documentation("multiplies then divides (n1 x n2) / n3, returning the remainder n n4 and quotient in n5", stackEffect = "( n1 n2 n3 -- n4 n5)")
  val `*/MOD` = dataStack(arity3stackOp2[Int](_ * _ / _)(_ * _ / _))
}
