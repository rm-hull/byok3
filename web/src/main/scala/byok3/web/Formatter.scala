package byok3.web

object Formatter {

  private val ansi = "\u001b\\[(\\d+;?)+m".r

  def stripAnsi(text: String) = ansi.replaceAllIn(text, "")
}
