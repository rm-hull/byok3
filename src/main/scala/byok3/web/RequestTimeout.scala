package byok3.web

import akka.util.Timeout
import com.typesafe.config.Config

trait RequestTimeout {
  import scala.concurrent.duration._
  def requestTimeout(config: Config): Timeout = {
    val t = config.getString("http.request-timeout")
    val d = Duration(t)
    FiniteDuration(d.length, d.unit)
  }
}
