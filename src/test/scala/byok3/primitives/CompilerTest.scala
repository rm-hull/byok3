package byok3.primitives

import cats.implicits._

class CompilerTest extends PrimitivesTestBase {

  test("should post-increment IP and push to the stack") {
    val ctx = Compiler.`(LIT)`.runS(emptyContext).get
    ctx.ds shouldEqual List(emptyContext.reg.ip)
    ctx.reg.ip shouldEqual emptyContext.reg.ip + 1
  }
}
