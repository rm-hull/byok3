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

package byok3

import byok3.data_structures.Context._
import byok3.data_structures.MachineState._
import byok3.data_structures.Stack._
import byok3.data_structures._
import byok3.implicits._
import byok3.primitives.Compiler._
import byok3.types.{AppState, Word}
import cats.data.StateT._
import cats.implicits._

import scala.util.Try


/**
  * Outer interpreter
  */
object Interpreter extends Executor {

  private val radixPrefix = Map('#' -> 10, '$' -> 16, '%' -> 2)

  private def parsePrefix(value: String) = {
    val radix = radixPrefix.get(value.charAt(0))
    radix.toTry(Error(-13, value))
      .flatMap(r => Try(Integer.parseInt(value.substring(1), r)))
  }

  private def toNumber(value: String, radix: Int) =
    Try(Integer.parseInt(value, radix))
      .orElse(parsePrefix(value))
      .getOrElse(throw Error(-13, value))

  private def pushNumber(token: Word) = for {
    status <- machineState
    base <- deref("BASE")
    n = toNumber(token, base)
    _ <- if (status == Smudge) literal(n) else dataStack(push(n))
  } yield ()

  private def runOrCompile(xt: ExecutionToken) = for {
    status <- machineState
    _ <- if (xt.immediate || status != Smudge) xt.effect else xt.compile
  } yield ()

  private def assemble: AppState[Unit] =
    get[Try, Context].flatMap { ctx =>
      ctx.input match {
        case Token(token, _, _) =>
          ctx.find(token)
            .map(xt => runOrCompile(xt))
            .getOrElse(pushNumber(token))
        case _ => pure(())
      }
    }

  override def step: AppState[Boolean] = for {
    _ <- assemble
    token <- nextToken()
  } yield token == EndOfData

  def apply(text: String): AppState[Unit] = for {
    _ <- input(text)
    _ <- modify[Try, Context](_.reset)
    _ <- modify(run)
  } yield ()
}