package byok3.data_structures

sealed trait Word {
  val value: Int
}

case class Data(value: Int) extends Word
case class Address(value: Int) extends Word