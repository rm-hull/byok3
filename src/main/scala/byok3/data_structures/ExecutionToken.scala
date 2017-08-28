package byok3.data_structures

import byok3.Executor
import byok3.annonation.Documentation
import byok3.data_structures.Context._
import byok3.data_structures.CoreMemory.{inc, peek}
import byok3.data_structures.Dictionary.{addressOf, instruction}
import byok3.data_structures.Stack.push
import byok3.primitives.Compiler
import byok3.primitives.FlowControl.__NEST
import byok3.types._
import cats.data.StateT._
import cats.implicits._


sealed trait ExecutionToken {
  val name: Word
  val effect: AppState[Unit]
  val immediate: Boolean = false
  val internal: Boolean = false

  def markAsImmediate: ExecutionToken = ???

  def compile = for {
    xt <- dictionary(addressOf(name))
    _ <- Compiler.compile(xt)
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

case class Variable(name: Word, addr: Address) extends ExecutionToken {
  override val effect = dataStack(push(addr))
}

/**
  * Inner interpreter
  */
case class UserDefined(name: Word, addr: Address, override val immediate: Boolean = false)
  extends ExecutionToken with Executor {

  override def markAsImmediate = copy(immediate = true)

  override val effect = for {
    xt <- memory(peek(addr))
    _ <- W(addr)
    _ <- XT(xt)
    rsEmpty <- returnStack(inspect(_.isEmpty))
    _ <- if (rsEmpty) modify(run) else __NEST
  } yield ()

  override def step = for {
    xt <- XT()
    instr <- dictionary(instruction(xt))
    _ <- exec(instr.name)
    ip <- IP()
    next <- memory(peek(ip))
    _ <- IP(inc(ip))
    _ <- XT(next)
    rsEmpty <- returnStack(inspect(_.isEmpty))
  } yield rsEmpty
}