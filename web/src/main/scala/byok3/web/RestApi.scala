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



