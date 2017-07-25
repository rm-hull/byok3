package byok3

import byok3.data_structures.ExecutionToken

package object types {

  type Data = Int
  type Address = Int
  type Word = String

  type Stack[A] = List[A]
  type AddressSpace = Map[Address, Data]

  type Dictionary = Map[Word, ExecutionToken]
}
