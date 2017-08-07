package byok3.data_structures

object MachineState {

  sealed abstract class Value(i: Int) {
    val value: Int = this.i
  }

  case object OK extends Value(0)

  case object Smudge extends Value(1)

  def states: Seq[Value] = Seq(OK, Smudge)

  def apply(value: Int): Value =
    states.find(v => v.value == value).getOrElse(throw new IllegalArgumentException)
}