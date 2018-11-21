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

package byok3

import java.time.LocalDate

import byok3.AnsiColor._

object Banner {

  private val logo =
    """|__/XXXXXXXXXXXXX____/XXX________/XXX____/XXXXX________/XXX________/XXX_
       | _X/XXX/////////XXX_X///XXX____/XXX/___/XXX///XXX_____X/XXX_____/XXX//__
       |  _X/XXX_______X/XXX___X///XXX/XXX/___/XXX/__X///XXX___X/XXX__/XXX//_____
       |   _X/XXXXXXXXXXXXXX______X///XXX/____/XXX______X//XXX__X/XXXXXX//XXX_____
       |    _X/XXX/////////XXX_______X/XXX____X/XXX_______X/XXX__X/XXX//_X//XXX____
       |     _X/XXX_______X/XXX_______X/XXX____X//XXX______/XXX___X/XXX____X//XXX___
       |      _X/XXX_______X/XXX_______X/XXX_____X///XXX__/XXX/____X/XXX_____X//XXX__
       |       _X/XXXXXXXXXXXXX/________X/XXX_______X///XXXXX/______X/XXX______X//XXX_
       |        _X/////////////__________X///__________X/////________X///________X///__
    """.stripMargin.map {
      case '_' => s"${RESET}${DARK_GREY}_"
      case '/' => s"${RESET}${GREEN}${BOLD}/"
      case 'X' => s"${RESET}${GREEN}${BOLD}\\"
      case other => other
    }.mkString

  def apply(version: String = s"${BuildInfo.version}-${BuildInfo.gitCommitHash.substring(0, 8)}", now: LocalDate = LocalDate.now) =
    s"""|
        |${logo}
        |${RESET}${LIGHT_GREY}===============================================================================
        |${RESET}${BOLD}              ## BYOK3: Forth machine, version: ${Option(version).getOrElse("<not-set>")} ##
        |${RESET}${LIGHT_GREY}      This program (c) ${now.getYear} Richard Hull, published under the MIT License.
        |     To read the licence, type LICENSE <enter>. For help, type HELP <enter>.
        |===============================================================================""".stripMargin
}
