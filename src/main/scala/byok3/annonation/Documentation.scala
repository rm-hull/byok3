package byok3.annonation

import scala.annotation.StaticAnnotation

case class Documentation(value: String, stackEffect: String) extends StaticAnnotation
