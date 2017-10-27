import byok3.data_structures.Context
import byok3.data_structures.Source.USER_INPUT_DEVICE


package object byok3 {

  val emptyContext = Context(0x100000).eval("include forth/system.fth", USER_INPUT_DEVICE)
}
