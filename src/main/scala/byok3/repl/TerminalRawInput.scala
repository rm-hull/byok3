package byok3.repl

import byok3.data_structures.RawInput
import org.jline.terminal.Terminal

case class TerminalRawInput(terminal: Terminal) extends RawInput {

  override def read(timeout: Int = 0): Int = {
    terminal.enterRawMode()
    terminal.reader().read(timeout)
  }
}
