package byok3.primitives

import byok3.Stack._

object Arithmetics {

  val + = arity2stackOp(_ + _)
  val - = arity2stackOp(_ - _)
  val * = arity2stackOp(_ * _)
  val / = arity2stackOp(_ / _)

  val `1+` = arity1stackOp(_ + 1)
  val `1-` = arity1stackOp(_ - 1)
  val `2+` = arity1stackOp(_ + 2)
  val `2-` = arity1stackOp(_ - 2)
  val `2/` = arity1stackOp(_ / 2)

  val ABS = arity1stackOp(math.abs)
  val NEGATE = arity1stackOp(- _)
  val MIN = arity2stackOp(_ min _)
  val MAX = arity2stackOp(_ max _)

  val `/MOD` = arity2stackOp2(_ % _)(_ / _)
}
