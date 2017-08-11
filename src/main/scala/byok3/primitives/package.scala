package byok3

package object primitives {

  implicit def truth(condition: Boolean) = if (condition) -1 else 0

  def unsigned(n: Int) = if (n < 0) n + 0x80000000 else n
}
