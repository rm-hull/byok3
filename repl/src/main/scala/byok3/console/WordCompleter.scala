/*
 * Copyright (c) 2017 Richard Hull
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package byok3.console

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
      case v: Variable => new Candidate(v.name, v.name, "variable", s"value: ${ctx.map(_.mem.peek(v.addr)).getOrElse("unknown")}", null, key, true)
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
