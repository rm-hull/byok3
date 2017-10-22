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

package byok3.web.actors

import akka.actor._
import byok3.AnsiColor._
import byok3.Banner
import byok3.data_structures.Context
import byok3.data_structures.MachineState.BYE
import byok3.helpers._

import scala.concurrent.ExecutionContext


object StackMachine {
  def props(name: String)(implicit ec: ExecutionContext) = Props(new StackMachine(name, root))

  val systemLibs = Seq("forth/system.fth")

  val root = systemLibs.map(load)
    .reduce(_ andThen _)
    .apply(Context(0x50000))
    .copy(rawConsoleInput = None)

  private def load(filename: String)(ctx: Context) =
    ctx.eval(s"include $filename")
}


class StackMachine(name: String, var ctx: Context)(implicit val ec: ExecutionContext)
  extends Actor with ActorLogging {

  private var printBanner = true

  override def preStart() =
    log.info(s"Starting stack machine: $self")

  override def receive = {
    case input: String => {

      log.info(s"$name: $input (sender = ${sender()})")

      sender() ! streamOutput {
        if (printBanner) {
          println(Banner())
          printBanner = false
        }

        print(MID_GREY)
        ctx = ctx.eval(input)
        print(ctx.prompt)

        if (ctx.status == Right(BYE))
          context.stop(self)
      }
    }
  }
}