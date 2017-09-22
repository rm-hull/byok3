package byok3.web

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import com.typesafe.config.ConfigFactory

import scala.io.StdIn

object Server extends RequestTimeout {

  def main(args: Array[String]) {

    val config = ConfigFactory.load()
    val host = config.getString("http.host")
    val port = config.getInt("http.port")

    implicit val system = ActorSystem("byok3")
    implicit val materializer = ActorMaterializer()
    implicit val executionContext = system.dispatcher

    val api = new RestApi(system, requestTimeout(config)).routes
    val bindingFuture = Http().bindAndHandle(api, host, port)

    println(s"Server online at http://$host:$port/\nPress RETURN to stop...")
    StdIn.readLine() // let it run until user presses return
    bindingFuture
      .flatMap(_.unbind()) // trigger unbinding from the port
      .onComplete(_ => system.terminate()) // and shutdown when done
  }
}