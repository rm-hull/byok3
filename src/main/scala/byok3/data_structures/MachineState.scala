package byok3.data_structures

sealed trait MachineState
case object OK extends MachineState
case object Smudge extends MachineState
case class Error(errno: Int, message: String) extends RuntimeException(message.trim) with MachineState

case object Error {

  private val sysErrors = Map(
    0 -> "undefined",
    -1 -> "general abort",
    -2 -> "for abort\"",
    -3 -> "stack overflow",
    -4 -> "stack underflow",
    -5 -> "return stack overflow",
    -6 -> "return stack underflow",
    -7 -> "do loops nested too deeply",
    -8 -> "dictionary overflow",
    -9 -> "invalid memory address",
    -10 -> "division by zero",
    -11 -> "result out of range",
    -12 -> "argument type mismatch",
    -13 -> "word not found",
    -14 -> "use only during compilation",
    -15 -> "invalid forget",
    -16 -> "attempt to use zero-length string as name",
    -17 -> "pictured numeric ouput string overflow",
    -18 -> "pictured numeric ouput string overflow",
    -19 -> "word name too long",
    -20 -> "write to a read-only location",
    -21 -> "unsupported operation",
    -22 -> "unstructured",
    -23 -> "address alignment exception",
    -24 -> "invalid numeric argument",
    -25 -> "return stack imbalance",
    -26 -> "loop parameters unavailable",
    -27 -> "invalid recursion",
    -28 -> "user interrupt",
    -29 -> "compiler nesting",
    -30 -> "obsolescent feature",
    -31 -> ">BODY used on non-CREATEd definition",
    -32 -> "invalid name argument",
    -33 -> "Block read exception",
    -34 -> "Block write exception",
    -35 -> "Invalid block number",
    -36 -> "Invalid file position",
    -37 -> "File I/O exception",
    -38 -> "File not found")

  def apply(errno: Int): Error =
    apply(errno, "")

  def apply(errno: Int, additionalInfo: String): Error = {
    val msg = if (errno > 0) "user defined error" else sysErrors.getOrElse(errno, "undefined")
    new Error(errno, s"$msg: $additionalInfo")
  }
}
