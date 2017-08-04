package byok3.repl

import org.jline.reader.ParsedLine
import org.jline.reader.Parser.ParseContext
import org.jline.reader.impl.DefaultParser


class UpperCaseParser extends DefaultParser {

  override def parse(line: String, cursor: Int, context: ParseContext): ParsedLine =
    super.parse(line.toUpperCase, cursor, context)
}
