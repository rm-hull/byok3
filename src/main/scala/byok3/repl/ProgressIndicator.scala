package byok3.repl

object ProgressIndicator {
  private val indicators = List("|", "/", "-", "\\")

  def apply(line: Int) = {
    indicators(line / 10 % indicators.length)
  }
}
