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
import akka.http.scaladsl.marshalling.{Marshaller, ToResponseMarshaller}
import akka.http.scaladsl.model.ContentTypes._
import akka.http.scaladsl.model.HttpEntity.ChunkStreamPart
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.model._
import akka.http.scaladsl.model.headers._
import akka.http.scaladsl.server.Directives._
import akka.pattern.ask
import akka.stream.scaladsl.Source
import akka.util.Timeout
import byok3.BuildInfo
import byok3.web.actors._

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

  @volatile lazy val supervisor = createSupervisor()

  def evaluate(session: Option[String], line: String) =
    supervisor.ask(KeyboardInput(session, line)).mapTo[Event]
}


trait RestRoutes extends SupervisorAPI {

  implicit val toResponseMarshaller: ToResponseMarshaller[Source[String, Any]] =
    Marshaller.opaque { lines =>
      val data = lines.map(line => ChunkStreamPart("\r" + line + "\n"))
      HttpResponse(entity = HttpEntity.Chunked(`text/plain(UTF-8)`, data))
    }

  private val version =
    path("api" / "version") {
      get {
        complete(HttpEntity(ContentTypes.`application/json`, BuildInfo.toJson))
      }
    }

  private val sendCommand =
    path("api") {
      post {
        entity(as[String]) { input =>
          optionalCookie("session") { cookie =>
            onSuccess(evaluate(cookie.map(_.value), input)) {
              case ProgramOutput(Some(session), output) =>
                setCookie(HttpCookiePair("session", session).toCookie()) {
                  complete {
                    Source(output)
                  }
                }

              case ProgramOutput(None, _) | UnknownSession =>
                deleteCookie("session") {
                  complete(NotFound)
                }
            }
          }
        }
      }
    }

  val routes = version ~ sendCommand
}



