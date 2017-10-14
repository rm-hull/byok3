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

import byok3.Interpreter
import byok3.annonation.{Documentation, Immediate}
import byok3.data_structures.Context._
import byok3.data_structures.CoreMemory._
import byok3.data_structures.Dictionary._
import byok3.data_structures.MachineState.{OK, Smudge}
import byok3.data_structures.Stack.{pop, push}
import byok3.data_structures._
import byok3.helpers._
import byok3.primitives.Memory.{comma, PAD}
import byok3.types.AppState
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

  @Documentation("Begin compilation of headerless secondary", stackEffect = "( -- xt )")
  val `:NONAME` = for {
    status <- machineState
    _ <- guard(status != Smudge, Error(-29)) // compiler nesting
    nest <- dictionary(addressOf("__NEST"))
    addr <- comma(nest)
    word = Anonymous(addr)
    _ <- dictionary(add(word))
    xt <- dictionary(addressOf(word.name))
    _ <- dataStack(push(xt))
    _ <- machineState(Smudge)
  } yield ()

  private def addToDictionary(word: Option[UserDefined]): AppState[Unit] = word match {
    case None => noOp
    case Some(userDefinedWord) => for {
      dp <- DP()
      wordSize = dp - userDefinedWord.addr
      _ <- dictionary(add(userDefinedWord.copy(size = Some(wordSize))))
      _ <- modify[Try, Context](_.copy(compiling = None))
    } yield ()
  }

  @Immediate
  @Documentation("End the current definition, allow it to be found in the dictionary and enter interpretation state, consuming colon-sys", stackEffect = "( C: colon-sys -- )")
  val `;` = for {
    status <- machineState
    _ <- guard(status == Smudge, Error(-14)) // used only during compilation
    exit <- dictionary(addressOf("EXIT"))
    _ <- comma(exit)
    userDefinedWord <- inspect[Try, Context, Option[UserDefined]](_.compiling)
    _ <- addToDictionary(userDefinedWord)
    _ <- machineState(OK)
  } yield ()

  @Documentation("Make the most recent definition an immediate word", stackEffect = "( -- )")
  val IMMEDIATE = for {
    lastWord <- dictionary(last)
    _ <- dictionary(replace(lastWord.markAsImmediate))
  } yield ()

  @Documentation("", stackEffect = "( -- xt )")
  val LATEST = for {
    state <- machineState
    offset = if (state == Smudge) 0 else 1
    xt <- dictionary(inspect(_.length - offset))
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

  @Documentation("the size of the body corresponding to xt", stackEffect = "( xt -- sz )")
  val `>SIZE` = for {
    xt <- dataStack(pop)
    instr <- dictionary(instruction(xt))
    size = instr.size.getOrElse { throw Error(-21) }  // unsupported operation
    _ <- dataStack(push(size))
  } yield ()

  @Documentation("the address and length of the name of the execution token", stackEffect = "( xt -- len a-addr)")
  val `NAME>` = for {
    _ <- PAD
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

  @Documentation("Restores the previous definition (if any) for a word. Use with caution", stackEffect = "( \"<spaces>name\" -- )")
  val FORGET = for {
    token <- nextToken()
    name = token.value.toUpperCase
    _ <- guard(name.nonEmpty, Error(-16))
    _ <- dictionary(forget(name))
  } yield ()

  @Documentation("Save the current input source specification. Store minus-one (-1) in SOURCE-ID if it is present. Make the string described by c-addr and u both the input source and input buffer, set >IN to zero, and interpret. When the parse area is empty, restore the prior input source specification. Other stack effects are due to the words EVALUATEd", stackEffect = "( i * x c-addr u -- j * x )")
  val EVALUATE = for {
    // TODO: set SOURCE-ID
    u <- dataStack(pop)
    addr <- dataStack(pop)
    input <- inspect[Try, Context, Tokenizer](_.input)
    text <- memory(fetch(addr, u))
    _ <- Interpreter(text)
    _ <- modify[Try, Context](_.copy(input = input))
  } yield ()
}
