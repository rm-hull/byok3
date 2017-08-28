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

object Comparison {

  @Documentation("compares top two stack elements, returns true flag if equal, false otherwise", stackEffect = "( x1 x2 -- f )")
  val `=` = dataStack(arity2stackOp(_ == _))

  @Documentation("compares top two stack elements, returns true flag if different, false otherwise", stackEffect =  "( x1 x2 -- f )")
  val `<>` = dataStack(arity2stackOp(_ != _))

  @Documentation("compares signed numbers n1 with n2, returns true if n1 is less than n2", stackEffect = "( n1 n2 -- f )")
  val `<` = dataStack(arity2stackOp(_ < _))

  @Documentation("compares signed numbers n1 with n2, returns true if n1 is greater than n2", stackEffect = "( n1 n2 -- f )")
  val `>` = dataStack(arity2stackOp(_ > _))

  @Documentation("compares unsigned numbers u1 with u2, returns true if u1 is less than u2", stackEffect = "( u1 u2 -- f )")
  val `U<` = dataStack(arity2stackOp((a, b) => unsigned(a) < unsigned(b)))

  @Documentation("compares unsigned numbers u1 with u2, returns true if u1 is greater than u2", stackEffect = "( u1 u2 -- f )")
  val `U>` = dataStack(arity2stackOp((a, b) => unsigned(a) > unsigned(b)))

  @Documentation("return a true flag if value of n is negative", stackEffect = "( n -- f )")
  val `0<` = dataStack(arity1stackOp(_ < 0))

  @Documentation("return a true flag if value of x is zero", stackEffect = "( x -- f )")
  val `0=` = dataStack(arity1stackOp(_ == 0))

  @Documentation("return a true flag if value of x is not zero", stackEffect = "( x -- f )")
  val `0<>` = dataStack(arity1stackOp(_ != 0))

  @Documentation( "return a true flag if value of x is greater than zero", stackEffect = "( x -- f )")
  val `0>` = dataStack(arity1stackOp(_ > 0))

  @Documentation("return a true flag if x1 is in the range of x2 ... x3-1", stackEffect = "( x1 x2 x3 -- f )")
  val WITHIN = dataStack(arity3stackOp((x1, x2, x3) => x1 >= x2 && x1 < x3))
}
