package byok3.primitives

import byok3.annonation.Documentation
import byok3.data_structures.Context._
import byok3.data_structures.Stack._

object Comparison {

  private implicit def truth(condition: Boolean) = if (condition) -1 else 0

  @Documentation("compares top two stack elements, returns true flag if equal, false otherwise", stackEffect = "( x1 x2 -- f )")
  val `=` = dataStack(arity2stackOp(_ == _))

  @Documentation("compares top two stack elements, returns true flag if different, false otherwise", stackEffect =  "( x1 x2 -- f )")
  val <> = dataStack(arity2stackOp(_ != _))

  @Documentation("compares signed numbers n1 with n2, returns true if n1 is less then n2.", stackEffect = "( n1 n2 -- f )")
  val < = dataStack(arity2stackOp(_ < _))

  @Documentation("compares signed numbers n1 with n2, returns true if n1 is greater then n2", stackEffect = "( n1 n2 -- f )")
  val > = dataStack(arity2stackOp(_ > _))

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


/*


arity1stackop(__ISNEG, truth(x1 < 0))
arity1stackop(__ISZERO, truth(x1 == 0))
arity1stackop(__ISNOTZERO, truth(x1 != 0))
arity1stackop(__ISPOS, truth(x1 > 0))
arity3stackop(__WITHIN, truth(x1 >= x2 && x1 < x3))

"U<", __ULT,  "( u1 u2 -- f )", "compares unsigned numbers u1 with u2, returns true if n1 is lower then n2.");
"U>", __UGT,  "( u1 u2 -- f )", "compares unsigned numbers u1 with u2, returns true if n1 is higher then n2.");
"0<", __ISNEG, "( n -- f )", "return a true flag if value of n is negative.");

"0=", __ISZERO, "( x -- f )", "return a true flag if value of x is zero.");
"0<>", __ISNOTZERO, "( x -- f )", "return a true flag if value of x is not zero.");
"0>", __ISPOS,  "( n -- f )", "return a true flag if value of x is greater than zero.");
"WITHIN", __WITHIN, "( x1 x2 x3 -- f )", "return a true flag if x1 is in the range of x2 ... x3-1.");

    // TODO: Move into system.fth
    add_constant(ctx, "FALSE", 0);
    add_constant(ctx, "TRUE", -1);
}

 */
}
