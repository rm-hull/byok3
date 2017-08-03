package byok3

import scala.util.{Failure, Success, Try}

package object implicits {

  implicit class OptionOps[A](opt: Option[A]) {
    def toTry(ex: Exception): Try[A] =
      opt.map(Success(_)).getOrElse(Failure(ex))
  }

}