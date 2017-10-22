import byok3.data_structures.Context


package object byok3 {

  val emptyContext = Context(0x100000).eval("include forth/system.fth")
}
