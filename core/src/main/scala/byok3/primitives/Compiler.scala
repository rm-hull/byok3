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

package byok3.primitives

import byok3.annonation.{Documentation, Immediate}
import byok3.data_structures.Context._
import byok3.data_structures.CoreMemory._
import byok3.data_structures.Dictionary._
import byok3.data_structures.MachineState.{OK, Smudge}
import byok3.data_structures.Stack.{pop, push}
import byok3.data_structures._
import byok3.helpers._
import byok3.implicits._
import byok3.primitives.Memory.comma
import cats.data.StateT._
import cats.implicits._

import scala.util.Try

object Compiler {

  def compile(ns: Int*) = sequence(ns.map(comma): _*)

  def literal(n: Int) = for {
    lit <- dictionary(addressOf("(LIT)"))
    _ <- compile(lit, n)
  } yield ()

  @Immediate
  @Documentation("Append the run-time semantics to the current definition", stackEffect = "Compilation: ( x -- ), Runtime: ( -- x )")
  val LITERAL = for {
    status <- machineState
    _ <- guard(status == Smudge, Error(-14)) // used only during compilation
    x <- dataStack(pop)
    _ <- literal(x)
  } yield ()

  @Documentation("Enter compilation state and start the current definition, producing colon-sys", stackEffect = "( C: \"<spaces>name\" -- colon-sys )")
  val `:` = for {
    status <- machineState
    _ <- guard(status != Smudge, Error(-29)) // compiler nesting
    token <- nextToken()
    nest <- dictionary(addressOf("__NEST"))
    addr <- comma(nest)
    _ <- modify[Try, Context](_.beginCompilation(token.value.toUpperCase, addr))
    _ <- machineState(Smudge)
  } yield ()

  @Immediate
  @Documentation("End the current definition, allow it to be found in the dictionary and enter interpretation state, consuming colon-sys", stackEffect = "( C: colon-sys -- )")
  val `;` = for {
    exit <- dictionary(addressOf("EXIT"))
    _ <- comma(exit)
    userDefinedWord <- inspectF[Try, Context, UserDefined](_.compiling.toTry(Error(-14))) // used only during compilation
    _ <- dictionary(add(userDefinedWord))
    _ <- modify[Try, Context](_.copy(compiling = None))
    _ <- machineState(OK)
  } yield ()

  @Documentation("Make the most recent definition an immediate word", stackEffect = "( -- )")
  val IMMEDIATE = for {
    lastWord <- dictionary(last)
    _ <- dictionary(replace(lastWord.markAsImmediate))
  } yield ()

  @Documentation("", stackEffect = "( -- xt )")
  val LATEST = for {
    xt <- dictionary(inspect(_.length - 1))
    _ <- dataStack(push(xt))
  } yield ()

  @Documentation("pfa is the parameter field address corresponding to xt", stackEffect = "( xt -- pfa )")
  val `>BODY` = for {
    xt <- dataStack(pop)
    instr <- dictionary(instruction(xt))
    pfa = instr match {
      case ud: UserDefined => ud.addr
      case _ => throw Error(-31)
    }
    _ <- dataStack(push(pfa))
  } yield ()

  @Documentation("the address and length of the name of the execution token", stackEffect = "( xt -- len a-addr)")
  val `NAME>` = for {
    _ <- exec("PAD")
    addr <- dataStack(pop)
    xt <- dataStack(pop)
    instr <- dictionary(instruction(xt))
    // TODO: copy to transient buffer instead of consuming heap
    _ <- memory(copy(addr, instr.name))
    len = instr.name.length
    _ <- dataStack(push(addr))
    _ <- dataStack(push(len))
  } yield ()

  @Documentation("Skip leading space delimiters. Parse name delimited by a space. Create a definition for name with the execution semantics: name Execution: ( -- a-addr )", stackEffect = "( \"<spaces>name\" -- )")
  val CREATE = for {
    token <- nextToken()
    name = token.value.toUpperCase
    _ <- guard(name.nonEmpty, Error(-16))
    nest <- dictionary(addressOf("__NEST"))
    addr <- comma(nest)
    _ <- literal(addr + (4 * CELL_SIZE))
    exit <- dictionary(addressOf("EXIT"))
    _ <- compile(exit)
    _ <- dictionary(add(UserDefined(name, addr)))
  } yield ()
}