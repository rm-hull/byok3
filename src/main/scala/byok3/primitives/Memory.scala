package byok3.primitives

import byok3.data_structures.Context._
import byok3.data_structures.Dictionary._
import byok3.data_structures.Memory.{peek, poke}
import byok3.data_structures.Registers._
import byok3.data_structures.Stack.{pop, push}
import byok3.data_structures.{Constant, Variable}
import byok3.types.Data
import cats.data.StateT._
import cats.implicits._

object Memory {

  def comma(value: Data) = for {
    addr <- register(postIncDP)
    _ <- memory(poke(addr, value))
  } yield addr

  val `!` = for {
    addr <- dataStack(pop)
    data <- dataStack(pop)
    _ <- memory(poke(addr, data))
  } yield ()

  val `@` = for {
    addr <- dataStack(pop)
    data <- memory(peek(addr))
    _ <- dataStack(push(data))
  } yield ()

  val +! = for {
    addr <- dataStack(pop)
    data <- memory(peek(addr))
    _ <- memory(poke(addr, data + 1))
  } yield ()

  val COMMA = for {
    data <- dataStack(pop)
    _ <- comma(data)
  } yield ()

  val `(LIT)` = for {
    ip <- register(postIncIP)
    _ <- dataStack(push(ip))
  } yield ()


  val VARIABLE = for {
    addr <- comma(0)
    token <- nextToken()
    _ <- dictionary(add(Variable(token.value, addr)))
  } yield ()

  val CONSTANT = for {
    value <- dataStack(pop)
    token <- nextToken()
    _ <- dictionary(add(Constant(token.value, value)))
  } yield ()

  val PARSE = for {
    tib <- register(inspect(_.tib))
    ascii <- dataStack(pop)
    token <- nextToken(delim = ascii.toChar.toString)
    _ <- dataStack(push(token.value.length))
    _ <- dataStack(push(tib + token.offset - token.value.length - 1))
  } yield ()

  val DP = for {
    dp <- register(inspect(_.dp))
    _ <- dataStack(push(dp))
  } yield ()

  val HERE = DP

  val TIB = for {
    tib <- register(inspect(_.tib))
    _ <- dataStack(push(tib))
  } yield ()


}
