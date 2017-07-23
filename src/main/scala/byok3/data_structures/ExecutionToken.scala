package byok3.data_structures

import cats.data.State

sealed trait ExecutionToken {
  val effect: State[Context, Unit]
}

case class Primitive(effect: State[Context, Unit]) extends ExecutionToken


