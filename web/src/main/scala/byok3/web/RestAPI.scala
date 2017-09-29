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

import akka.actor.{ActorRef, ActorSystem}
import akka.http.scaladsl.model.ContentTypes._
import akka.http.scaladsl.model.MediaTypes._
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.model._
import akka.http.scaladsl.model.headers._
import akka.http.scaladsl.server.Directives._
import akka.pattern.ask
import akka.util.Timeout
import byok3.AnsiColor
import byok3.web.actors.{Event, Supervisor, Text, UnknownSession}
import nl.grons.metrics.scala.DefaultInstrumented

import scala.concurrent.ExecutionContext


class RestAPI(system: ActorSystem, timeout: Timeout) extends RestRoutes {
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


trait RestRoutes extends SupervisorAPI with DefaultInstrumented {

  private[this] def extractAccept: PartialFunction[HttpHeader, MediaType] = {
    case Accept(`text/html`) => `text/html`
    case Accept(_) => `text/plain`
  }

  healthCheck("alive") { true }

  private[this] val eval = metrics.timer("byok3")

  val routes =
    path("byok3") {
      post {
        eval.time {
          entity(as[String]) { input =>
            optionalCookie("session") { cookie =>
              onSuccess(evaluate(cookie.map(_.value), input)) {
                case Text(Some(session), output) =>
                  setCookie(HttpCookiePair("session", session).toCookie()) {
                    headerValueByName("Accept") {
                      case "text/html" => complete(HttpEntity(`text/html(UTF-8)`, AnsiColor.strip(output)))
                      case _ => complete(HttpEntity(`text/plain(UTF-8)`, output))
                    }
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
}



