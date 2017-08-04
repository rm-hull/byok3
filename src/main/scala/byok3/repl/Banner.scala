package byok3.repl

import java.time.LocalDate

import byok3.repl.AnsiColor._

object Banner {

  private val logo =
    """|__/XXXXXXXXXXXXX____/XXX________/XXX____/XXXXX________/XXX________/XXX_
       | _X/XXX/////////XXX_X///XXX____/XXX/___/XXX///XXX_____X/XXX_____/XXX//__
       |  _X/XXX_______X/XXX___X///XXX/XXX/___/XXX/__X///XXX___X/XXX__/XXX//_____
       |   _X/XXXXXXXXXXXXXX______X///XXX/____/XXX______X//XXX__X/XXXXXX//XXX_____
       |    _X/XXX/////////XXX_______X/XXX____X/XXX_______X/XXX__X/XXX//_X//XXX____
       |     _X/XXX_______X/XXX_______X/XXX____X//XXX______/XXX___X/XXX____X//XXX___
       |      _X/XXX_______X/XXX_______X/XXX_____X///XXX__/XXX_____X/XXX_____X//XXX__
       |       _X/XXXXXXXXXXXXX/________X/XXX_______X///XXXXX/______X/XXX______X//XXX_
       |        _X/////////////__________X///__________X/////________X///________X///__
    """.stripMargin.map {
      case '_' => s"${DARK_GREY}_"
      case '/' => s"${GREEN}${BOLD}/"
      case 'X' => s"${GREEN}${BOLD}\\"
      case other => other
    }.mkString

  def apply(version: String = getClass.getPackage.getImplementationVersion, now: LocalDate = LocalDate.now) =
    s"""|
        |${logo}
        |${RESET}${LIGHT_GREY}===============================================================================
        |${RESET}${BOLD}                ## BYOK3: Forth machine, version: ${Option(version).getOrElse("<not-set>")} ##
        |${RESET}${LIGHT_GREY}      This program (c) ${now.getYear} Richard Hull, published under the MIT License.
        |     To read the licence, type LICENSE <enter>. For help, type HELP <enter>.
        |===============================================================================""".stripMargin
}