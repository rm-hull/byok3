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

import byok3.data_structures.Context._
import byok3.data_structures.CoreMemory._
import byok3.data_structures.Error
import byok3.data_structures.MachineState.{OK, Smudge}
import byok3.data_structures.Stack._
import byok3.helpers._
import byok3.primitives.Compiler._
import byok3.primitives.Memory._
import cats.implicits._

import scala.util.Failure

class CompilerTest extends PrimitivesTestBase {

  test("literal should fail when not in compile mode") {
    val ops = sequence(machineState(OK), LITERAL)
    val ctx = ops.runS(emptyContext)
    ctx shouldEqual Failure(Error(-14))
  }

  test("literal should write (LIT) n when in compile mode") {
    val ops = sequence(machineState(Smudge), dataStack(push(32)), LITERAL, HERE)
    val ctx = ops.runS(emptyContext).get
    val here = ctx.ds.head
    ctx.dictionary.get(ctx.mem.peek(dec(dec(here)))).get.name shouldEqual "(LIT)"
    ctx.mem.peek(dec(here)) shouldEqual 32
  }
}
