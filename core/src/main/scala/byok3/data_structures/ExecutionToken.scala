/*
 * Copyright (c) 2017 Richard Hull
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package byok3.data_structures

import byok3.Executor
import byok3.annonation.Documentation
import byok3.data_structures.Context._
import byok3.data_structures.CoreMemory.{inc, peek}
import byok3.data_structures.Dictionary.{addressOf, instruction}
import byok3.data_structures.Stack.push
import byok3.primitives.Compiler
import byok3.primitives.FlowControl.__NEST
import byok3.primitives.IO.trace
import byok3.types._
import cats.data.StateT._
import cats.instances.try_._

import scala.util.Try


sealed trait ExecutionToken {
  val name: Word
  val effect: AppState[Unit]
  val immediate: Boolean = false
  val internal: Boolean = false
  val size: Option[Int] = None
  val doc: Option[Documentation] = None
  val position: Option[Position] = None
  val source: Option[String] = None

  def markAsImmediate: ExecutionToken = ???

  def compile = for {
    xt <- dictionary(addressOf(name))
    _ <- Compiler.compile(xt)
  } yield ()
}

sealed trait InnerInterpreter extends Executor {

  val addr: Address
  val immediate: Boolean

  val effect = for {
    xt <- memory(peek(addr))
    _ <- W(addr)
    _ <- XT(xt)
    rsEmpty <- inspect[Try, Context, Boolean](_.rs.isEmpty)
    _ <- if (rsEmpty || immediate) modify(run) else __NEST
  } yield ()

  override def step = for {
    xt <- XT()
    instr <- dictionary(instruction(xt))
    _ <- trace(instr.name)
    _ <- instr.effect
    ip <- IP()
    next <- memory(peek(ip))
    _ <- IP(inc(ip))
    _ <- XT(next)
    rsEmpty <- inspect[Try, Context, Boolean](_.rs.isEmpty)
  } yield rsEmpty
}


case class Primitive(name: Word,
                     effect: AppState[Unit],
                     override val immediate: Boolean,
                     override val internal: Boolean,
                     override val doc: Option[Documentation]) extends ExecutionToken

case class Constant(name: Word,
                    value: Data,
                    override val doc: Option[Documentation] = None,
                    override val position: Option[Position] = None) extends ExecutionToken {
  override val effect = dataStack(push(value))
}

case class Variable(name: Word,
                    addr: Address,
                    override val doc: Option[Documentation] = None,
                    override val position: Option[Position] = None) extends ExecutionToken {
  override val effect = dataStack(push(addr))
}

sealed trait ForthWord extends ExecutionToken with InnerInterpreter {
  val addr: Address

  def withSize(n: Int): ForthWord = ???
  def withSourceTokens(tokens: Seq[Tokenizer]): ForthWord = ???

  protected def reconstructSource(tokens: Seq[Tokenizer]) = {
    val lines = tokens.foldLeft(Seq(Seq.empty[Token])) {
      case (acc, token:Token) => acc.init :+ (acc.last :+ token)
      case (acc, _) => acc :+ Seq.empty[Token]
    }

    Some(lines.flatMap(reconstructLine).mkString("\n"))
  }

  private def reconstructLine(tokens: Seq[Token]) =
    tokens.headOption.map(_.in)
}

object ForthWord {
  def apply(name: Word, addr: Address, position: Option[Position], systemLib: Boolean) =
    if (systemLib)
      SystemDefined(name, addr, position)
    else
      UserDefined(name, addr, position)
}

case class UserDefined(name: Word,
                       addr: Address,
                       override val position: Option[Position],
                       override val immediate: Boolean = false,
                       override val size: Option[Int] = None,
                       override val source: Option[String] = None) extends ForthWord {

  override def withSize(n: Int) = copy(size = Some(n))
  override def withSourceTokens(tokens: Seq[Tokenizer]): ForthWord = copy(source = reconstructSource(tokens))
  override def markAsImmediate = copy(immediate = true)
}

case class SystemDefined(name: Word,
                         addr: Address,
                         override val position: Option[Position],
                         override val immediate: Boolean = false,
                         override val size: Option[Int] = None,
                         override val source: Option[String] = None) extends ForthWord {

  override def withSize(n: Int) = copy(size = Some(n))
  override def withSourceTokens(tokens: Seq[Tokenizer]): ForthWord = copy(source = reconstructSource(tokens))
  override def markAsImmediate = copy(immediate = true)
}

case class Anonymous(addr: Address) extends ExecutionToken with InnerInterpreter {
  val name = f"__anon_$addr%08X"
  override val internal = true
}