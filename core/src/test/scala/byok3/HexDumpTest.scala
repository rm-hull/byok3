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

import byok3.data_structures.CoreMemory
import byok3.helpers.capturingOutput
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers

class HexDumpTest extends AnyFunSuite with Matchers {

  private val mem = Range(0x00, 0xFF).foldLeft(CoreMemory(0x10000)) {
    (mem, i) => mem.char_poke(i + 17, i)
  }

  private val hexdump = new HexDump(mem)

  test("should print 2 column hex-dump") {
    val expected =
      """00000000:  00 00 00 00 00 00 00 00  00 00 00 00 00 00 00 00  |........ ........|
        |00000010:  00 00 01 02 03 04 05 06  07 08 09 0A 0B 0C 0D 0E  |........ ........|
        |00000020:  0F 10 11 12 13 14 15 16  17 18 19 1A 1B 1C 1D 1E  |........ ........|
        |00000030:  1F 20 21 22 23 24 25 26  27 28 29 2A 2B 2C 2D 2E  |. !"#$%& '()*+,-.|
        |00000040:  2F 30 31 32 33 34 35 36  37 38 39 3A 3B 3C 3D 3E  |/0123456 789:;<=>|
        |00000050:  3F 40 41 42 43 44 45 46  47 48 49 4A 4B 4C 4D 4E  |?@ABCDEF GHIJKLMN|
        |00000060:  4F 50 51 52 53 54 55 56  57 58 59 5A 5B 5C 5D 5E  |OPQRSTUV WXYZ[\]^|
        |00000070:  5F 60 61 62 63 64 65 66  67 68 69 6A 6B 6C 6D 6E  |_`abcdef ghijklmn|
        |00000080:  6F 70 71 72 73 74 75 76  77 78 79 7A 7B 7C 7D 7E  |opqrstuv wxyz{|}~|
        |00000090:  7F 80 81 82 83 84 85 86  87 88 89 8A 8B 8C 8D 8E  |........ ........|
        |000000A0:  8F 90 91 92 93 94 95 96  97 98 99 9A 9B 9C 9D 9E  |........ ........|
        |000000B0:  9F A0 A1 A2 A3 A4 A5 A6  A7 A8 A9 AA AB AC AD AE  |........ ........|
        |000000C0:  AF B0 B1 B2 B3 B4 B5 B6  B7 B8 B9 BA BB BC BD BE  |........ ........|
        |000000D0:  BF C0 C1 C2 C3 C4 C5 C6  C7 C8 C9 CA CB CC CD CE  |........ ........|
        |000000E0:  CF D0 D1 D2 D3 D4 D5 D6  D7 D8 D9 DA DB DC DD DE  |........ ........|
        |000000F0:  DF E0 E1 E2 E3 E4 E5 E6  E7 E8 E9 EA EB EC ED EE  |........ ........|
        |00000100:  EF F0 F1 F2 F3 F4 F5 F6  F7 F8 F9 FA FB FC FD FE  |........ ........|
        |00000110:  00 00 00 00 00 00 00 00  00 00 00 00 00 00 00 00  |........ ........|
        |""".stripMargin

    val actual = capturingOutput(hexdump.print(0x0000, 0x120))
    actual shouldEqual expected
  }

  test("should print aligned hex-dump") {
    val expected =
      """00000020:           12 13 14 15 16  17 18 19 1A 1B 1C 1D 1E  |   ..... ........|
        |00000030:  1F 20 21 22 23 24 25 26  27 28 29 2A 2B 2C 2D 2E  |. !"#$%& '()*+,-.|
        |00000040:  2F 30 31 32 33 34 35 36  37 38 39 3A 3B 3C 3D 3E  |/0123456 789:;<=>|
        |00000050:  3F 40 41 42 43 44 45 46  47 48 49 4A 4B 4C 4D 4E  |?@ABCDEF GHIJKLMN|
        |00000060:  4F 50 51 52 53 54 55 56  57 58 59 5A 5B 5C 5D 5E  |OPQRSTUV WXYZ[\]^|
        |00000070:  5F 60 61 62 63 64 65 66  67 68 69 6A 6B 6C 6D 6E  |_`abcdef ghijklmn|
        |00000080:  6F 70 71 72 73 74 75 76  77 78 79 7A 7B 7C 7D 7E  |opqrstuv wxyz{|}~|
        |00000090:  7F 80 81 82 83 84 85 86  87 88                    |........ ..      |
        |""".stripMargin

    val actual = capturingOutput(hexdump.print(0x0023, 0x77))
    actual shouldEqual expected
  }

  test("should print 1 column hex-dump") {
    val expected =
      """00000000:  00 00 00 00 00 00 00 00  |........|
        |00000008:  00 00 00 00 00 00 00 00  |........|
        |00000010:  00 00 01 02 03 04 05 06  |........|
        |00000018:  07 08 09 0A 0B 0C 0D 0E  |........|
        |00000020:  0F 10 11 12 13 14 15 16  |........|
        |00000028:  17 18 19 1A 1B 1C 1D 1E  |........|
        |00000030:  1F 20 21 22 23 24 25 26  |. !"#$%&|
        |00000038:  27 28 29 2A 2B 2C 2D 2E  |'()*+,-.|
        |00000040:  2F 30 31 32 33 34 35 36  |/0123456|
        |00000048:  37 38 39 3A 3B 3C 3D 3E  |789:;<=>|
        |00000050:  3F 40 41 42 43 44 45 46  |?@ABCDEF|
        |00000058:  47 48 49 4A 4B 4C 4D 4E  |GHIJKLMN|
        |00000060:  4F 50 51 52 53 54 55 56  |OPQRSTUV|
        |00000068:  57 58 59 5A 5B 5C 5D 5E  |WXYZ[\]^|
        |00000070:  5F 60 61 62 63 64 65 66  |_`abcdef|
        |00000078:  67 68 69 6A 6B 6C 6D 6E  |ghijklmn|
        |00000080:  6F 70 71 72 73 74 75 76  |opqrstuv|
        |00000088:  77 78 79 7A 7B 7C 7D 7E  |wxyz{|}~|
        |00000090:  7F 80 81 82 83 84 85 86  |........|
        |00000098:  87 88 89 8A 8B 8C 8D 8E  |........|
        |000000A0:  8F 90 91 92 93 94 95 96  |........|
        |000000A8:  97 98 99 9A 9B 9C 9D 9E  |........|
        |000000B0:  9F A0 A1 A2 A3 A4 A5 A6  |........|
        |000000B8:  A7 A8 A9 AA AB AC AD AE  |........|
        |000000C0:  AF B0 B1 B2 B3 B4 B5 B6  |........|
        |000000C8:  B7 B8 B9 BA BB BC BD BE  |........|
        |000000D0:  BF C0 C1 C2 C3 C4 C5 C6  |........|
        |000000D8:  C7 C8 C9 CA CB CC CD CE  |........|
        |000000E0:  CF D0 D1 D2 D3 D4 D5 D6  |........|
        |000000E8:  D7 D8 D9 DA DB DC DD DE  |........|
        |000000F0:  DF E0 E1 E2 E3 E4 E5 E6  |........|
        |000000F8:  E7 E8 E9 EA EB EC ED EE  |........|
        |00000100:  EF F0 F1 F2 F3 F4 F5 F6  |........|
        |00000108:  F7 F8 F9 FA FB FC FD FE  |........|
        |00000110:  00 00 00 00 00 00 00 00  |........|
        |00000118:  00 00 00 00 00 00 00 00  |........|
        |""".stripMargin

    val actual = capturingOutput(hexdump.print(0x0000, 0x120, columns = 1))
    actual shouldEqual expected
  }
}
