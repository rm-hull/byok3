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
