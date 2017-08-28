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

object BitLogic {

  @Documentation("bitwise and x1 with x2, return result x3", stackEffect = "( x1 x2 -- x3 )")
  val AND = dataStack(arity2stackOp(_ & _))

  @Documentation("bitwise or x1 with x2, return result x3", stackEffect = "( x1 x2 -- x3 )")
  val OR = dataStack(arity2stackOp(_ | _))

  @Documentation("bitwise exclusive-or x1 with x2, return result x3", stackEffect = "( x1 x2 -- x3 )")
  val XOR = dataStack(arity2stackOp(_ ^ _))

  @Documentation("return the bitwise complement of x1", stackEffect = "( x1 -- x2 )")
  val INVERT = dataStack(arity1stackOp(~ _))

  @Documentation("logical shift left u1 by u2 bits", stackEffect = "( u1 u2 -- u3 )")
  val LSHIFT = dataStack(arity2stackOp(_ << _))

  @Documentation("logical shift right u1 by u2 bits", stackEffect = "( u1 u2 -- u3 )")
  val RSHIFT = dataStack(arity2stackOp(_ >> _))
}
