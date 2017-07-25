package byok3.data_structures

import byok3.types.Address

case class Registers(dp: Address, ip: Address)


object Registers {
  def apply(): Registers = Registers(dp = 0x0000, ip = 0x2000)
}
