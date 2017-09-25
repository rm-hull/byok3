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

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.RouteConcatenation.concat
import akka.stream.ActorMaterializer
import akka.util.Timeout
import com.typesafe.config.{Config, ConfigFactory}

import scala.concurrent.duration.{Duration, FiniteDuration}

object Server extends RequestTimeout {

  def main(args: Array[String]) {

    val config = ConfigFactory.load()
    val host = config.getString("http.host")
    val port = config.getInt("http.port")

    implicit val system = ActorSystem("byok3")
    implicit val materializer = ActorMaterializer()
    implicit val executionContext = system.dispatcher

    val api = concat(Assets.routes, new RestAPI(system, requestTimeout(config)).routes)
    Http().bindAndHandle(api, host, port)

    println(s"Server online at http://$host:$port/")
    println(s"Press CTRL-C to stop...")
  }
}

trait RequestTimeout {
  def requestTimeout(config: Config): Timeout = {
    val t = config.getString("http.request-timeout")
    val d = Duration(t)
    FiniteDuration(d.length, d.unit)
  }
}