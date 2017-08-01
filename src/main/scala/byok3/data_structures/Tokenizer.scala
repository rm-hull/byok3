package byok3.data_structures

import byok3.types.Word

sealed trait Tokenizer {
  val value: Word
  val offset: Int
  val exhausted: Boolean
  def next(delim: String): Tokenizer
}

object Tokenizer {
  val delimiters = "[ \t\n]"

  def apply(in: String, delim: String = delimiters): Tokenizer =
    Token("", -1, in).next(delim)
}

case object EndOfData extends Tokenizer {
  override val value = ""
  override val offset = 0
  override val exhausted = true
  override def next(delim: String) = EndOfData
}

case class Token(value: String, offset: Int, in: String) extends Tokenizer {
  override def next(delim: String) = {
    val nextOffset = value.length + offset + 1
    if (nextOffset >= in.length) EndOfData
    else in.substring(nextOffset).split(delim).headOption match {
      case None => EndOfData
      case Some(t) => Token(t, nextOffset, in)
    }
  }

  override lazy val exhausted = in.endsWith(value) && in.length - offset == value.length
}


