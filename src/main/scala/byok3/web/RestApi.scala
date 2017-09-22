package byok3.web

import akka.actor.{ActorRef, ActorSystem}
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.model.headers.HttpCookiePair
import akka.http.scaladsl.model.{ContentTypes, HttpEntity}
import akka.http.scaladsl.server.Directives._
import akka.pattern.ask
import akka.util.Timeout
import byok3.web.Supervisor.{Event, Text, UnknownSession}

import scala.concurrent.ExecutionContext


class RestApi(system: ActorSystem, timeout: Timeout) extends RestRoutes {
  implicit val requestTimeout = timeout

  implicit def executionContext = system.dispatcher

  def createSupervisor = system.actorOf(Supervisor.props, Supervisor.name)
}

trait SupervisorAPI {
  def createSupervisor(): ActorRef
  implicit def executionContext: ExecutionContext
  implicit def requestTimeout: Timeout

  lazy val supervisor = createSupervisor()

  def evaluate(session: Option[String], line: String) =
    supervisor.ask(Text(session, line)).mapTo[Event]
}


trait RestRoutes extends SupervisorAPI {

  val routes =
    path("byok3") {
      post {
        entity(as[String]) { input =>
          optionalCookie("session") { cookie =>
            onSuccess(evaluate(cookie.map(_.value), input)) {
              case Text(Some(session), output) =>
                setCookie(HttpCookiePair("session", session).toCookie()) {
                  complete(HttpEntity(ContentTypes.`text/plain(UTF-8)`, output))
                }

              case Text(None, _) | UnknownSession =>
                deleteCookie("session") {
                  complete(NotFound)
                }
            }
          }
        }
      }
    }
}



