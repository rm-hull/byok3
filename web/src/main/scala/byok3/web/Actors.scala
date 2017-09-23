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

package byok3.web

import java.util.UUID

import akka.actor._
import akka.pattern.{ask, pipe}
import akka.util.Timeout
import byok3.web.Supervisor._

import scala.concurrent.ExecutionContext.Implicits.global

object Supervisor {
  def props(implicit timeout: Timeout) = Props(new Supervisor)
  def name = "supervisor"

  def forward(machine: ActorRef, session: String, input: String)(implicit timeout: Timeout) =
    machine.ask(input).mapTo[String].map(s => Text(Some(session), s))

  sealed trait Event
  case class Text(session: Option[String], text: String) extends Event
  case object UnknownSession extends Event
}

class Supervisor(implicit timeout: Timeout) extends Actor with ActorLogging {

  private def createStackMachine(name: String) =
    context.actorOf(StackMachine.props(name), name)

  override def preStart() = {
    log.info(s"Starting supervisor: $self")
  }

  def receive = {

    // Create a new stack machine
    case Text(None, input) => {
      val session = UUID.randomUUID.toString
      val machine = createStackMachine(session)
      val response = forward(machine, session, input)
      pipe(response) to sender()
    }

    // Forward onto correct stack machine
    case Text(Some(session), input) => {
      context.child(session).fold(sender() ! UnknownSession) { machine =>
        val response = forward(machine, session, input)
        pipe(response) to sender()
      }
    }
  }
}

object StackMachine {
  def props(name: String) = Props(new StackMachine(name))
}

class StackMachine(name: String) extends Actor with ActorLogging {

  override def preStart() = {
    log.info(s"Starting: $name")
    // TODO: create VM here
  }

  override def receive = {
    case s: String => {
      log.info(s"$name: $s (sender = ${sender()})")
      sender() ! s"Hello $s"
      // TODO: call out to VM
    }
  }
}
