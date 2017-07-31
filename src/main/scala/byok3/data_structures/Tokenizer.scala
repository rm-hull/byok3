package byok3.data_structures

import byok3.types.Word

sealed trait Tokenizer {
  val value: Word
  val offset: Int
  def next(delim: String): Tokenizer
}

object Tokenizer {
  val delimiters = "[ \t\n]"

  def apply(in: String, delim: String = delimiters): Tokenizer =
    Token("", in, 0).next(delim)
}

case object EndOfData extends Tokenizer {
  override val value = ""
  override val offset = 0
  override def next(delim: String) = EndOfData
}

case class Token(value: String, in: String, offset: Int) extends Tokenizer {
  override def next(delim: String) =
    if (offset >= in.length) EndOfData
    else {
      in.substring(offset).split(delim).headOption match {
        case None => EndOfData
        case Some(t) => {
          val newOffset = offset + t.length + 1
          Token(t, in, newOffset)
        }
      }
    }
}


