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
