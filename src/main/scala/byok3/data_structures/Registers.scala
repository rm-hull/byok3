package byok3.data_structures

case class Registers(dp: Address, ip: Address)


object Registers {
  def apply(): Registers = Registers(dp = Address(0x0000), ip = Address(0x2000))
}
