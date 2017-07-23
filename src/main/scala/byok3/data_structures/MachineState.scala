package byok3.data_structures

sealed trait MachineState
case object OK extends MachineState
case object Smudge extends MachineState
case object Error extends MachineState
