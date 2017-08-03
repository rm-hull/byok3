package byok3.repl

trait ExtendedAnsiColors {
  val LIGHT_GREY = "\u001b[38;5;252m"
  val MID_GREY = "\u001b[38;5;247m"
  val DARK_GREY = "\u001b[38;5;242m"
}

object AnsiColor extends scala.io.AnsiColor with ExtendedAnsiColors
