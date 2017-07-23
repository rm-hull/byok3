package byok3.primitives

import byok3.data_structures.Context._
import byok3.data_structures.Stack._

object Arithmetics {

  val + = dataStack(arity2stackOp(_ + _))
  val - = dataStack(arity2stackOp(_ - _))
  val * = dataStack(arity2stackOp(_ * _))
  val / = dataStack(arity2stackOp(_ / _))

  val `1+` = dataStack(arity1stackOp(_ + 1))
  val `1-` = dataStack(arity1stackOp(_ - 1))
  val `2+` = dataStack(arity1stackOp(_ + 2))
  val `2-` = dataStack(arity1stackOp(_ - 2))
  val `2/` = dataStack(arity1stackOp(_ / 2))

  val ABS = dataStack(arity1stackOp(math.abs))
  val NEGATE = dataStack(arity1stackOp(-_))
  val MIN = dataStack(arity2stackOp(_ min _))
  val MAX = dataStack(arity2stackOp(_ max _))

  val `/MOD` = dataStack(arity2stackOp2[Int](_ % _)(_ / _))
  val `*/` = dataStack(arity3stackOp[Int](_ * _ / _))
  val `*/MOD` = dataStack(arity3stackOp2[Int](_ * _ / _)(_ * _ / _))
}
