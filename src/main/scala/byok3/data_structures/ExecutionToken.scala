package byok3.data_structures

import byok3.annonation.Documentation
import byok3.data_structures.Context._
import byok3.data_structures.Dictionary.addressOf
import byok3.data_structures.Stack._
import byok3.primitives.{Compiler, Control}
import byok3.types.{Address, AppState, Data, Word}
import cats.implicits._

sealed trait ExecutionToken { self =>
  val name: Word
  val effect: AppState[Unit]
  val immediate: Boolean = false
  val internal: Boolean = false

  def markAsImmediate: ExecutionToken =
    throw Error(-21) // unsupported operation

  def addr: Address = ???

  val compile = for {
    addr <- dictionary(addressOf(name))
    _ <- Compiler.compile(addr)
  } yield ()

  val run = for {
    _ <- setCurrentXT(Some(self))
    _ <- effect
  } yield ()
}

case class Primitive(name: Word,
                     effect: AppState[Unit],
                     override val immediate: Boolean,
                     override val internal: Boolean,
                     doc: Option[Documentation]) extends ExecutionToken

case class Constant(name: Word, value: Data) extends ExecutionToken {
  override val effect = dataStack(push(value))
}

case class Variable(name: Word, override val addr: Address) extends ExecutionToken {
  override val effect = dataStack(push(addr))
}

case class UserDefined(name: Word,
                       override val addr: Address,
                       override val immediate: Boolean = false) extends ExecutionToken {

  override val effect = for {
    _ <- dataStack(push(-21))
    _ <- Control.THROW
  } yield () // FIXME  Not supported for now...

  override def markAsImmediate = copy(immediate = true)
}