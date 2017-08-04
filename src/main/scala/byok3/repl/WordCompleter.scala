package byok3.repl

import java.util
import java.util.UUID

import byok3.data_structures._
import org.jline.reader.{Candidate, Completer, LineReader, ParsedLine}


class WordCompleter extends Completer {
  private var ctx: Option[Context] = None

  def setContext(ctx: Context): Unit = {
    this.ctx = Some(ctx)
  }

  private def immediate(exeTok: ExecutionToken, doc: String) =
    if (exeTok.immediate) s"[IMMEDIATE] ${Option(doc).getOrElse("")}".trim else doc

  private def candidate(key: String, exeTok: ExecutionToken) =
    exeTok match {
      case p: Primitive => new Candidate(p.name, p.name, "built-in", immediate(p, p.doc.fold[String](null)(_.toString)), null, key, true)
      case v: Variable => new Candidate(v.name, v.name, "variable", s"value: ${ctx.get.mem.peek(v.addr)}", null, key, true)
      case c: Constant => new Candidate(c.name, c.name, "constant", s"constant: ${c.value}", null, key, true)
      case u: UserDefined => new Candidate(u.name, u.name, "user-defined", immediate(u, null), null, key, true)
    }

  override def complete(reader: LineReader, line: ParsedLine, candidates: util.List[Candidate]): Unit = {
    ctx.foreach { c =>
      c.dictionary.toMap.values.filterNot(_.internal).foreach { exeTok =>
        candidates.add(candidate(UUID.randomUUID.toString, exeTok))
      }
    }
  }
}
