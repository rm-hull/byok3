package byok3.data_structures

import byok3.annonation.Documentation
import byok3.data_structures.Context._
import byok3.data_structures.Stack._
import byok3.types.{Address, AppState, Data, Word}

sealed trait ExecutionToken {
  val name: Word
  val effect: AppState[Unit]
  val immediate: Boolean = false

  def markImmediate: ExecutionToken = throw new UnsupportedOperationException()
}

case class Primitive(name: Word,
                     effect: AppState[Unit],
                     override val immediate: Boolean,
                     override val internal: Boolean,
                     doc: Option[Documentation]) extends ExecutionToken

case class Constant(name: Word, value: Data) extends ExecutionToken {
  override val effect = dataStack(push(value))
}

case class Variable(name: Word, addr: Address) extends ExecutionToken {
  override val effect = dataStack(push(addr))
}