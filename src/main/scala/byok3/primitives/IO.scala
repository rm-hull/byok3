package byok3.primitives

import byok3.annonation.{Documentation, StackEffect}
import byok3.data_structures.Context._
import byok3.data_structures.Memory._
import byok3.data_structures.Stack._
import byok3.types.Stack
import cats.data.StateT.get
import cats.implicits._

import scala.util.Try

object IO {

  @Documentation("convert signed number n to string of digits, and output.")
  @StackEffect("( n -- )")
  val `.` = for {
    a <- dataStack(pop)
    _ <- output(print(s"$a "))
  } yield ()

  @Documentation("display stack contents.")
  @StackEffect("( -- )")
  val `.S` = for {
    stack <- dataStack(get[Try, Stack[Int]])
    _ <- output(print(stack.reverse.mkString(" ") + " "))
  } yield ()

  @Documentation("outputs ascii as character.")
  @StackEffect("( ascii -- )")
  val EMIT = for {
    ascii <- dataStack(pop)
    _ <- output(print(ascii.toChar))
  } yield ()


  @Documentation("outputs u space characters.")
  @StackEffect("( u -- )")
  val SPACES = for {
    n <- dataStack(pop)
    _ <- output(print(' ' * n))
  } yield ()

  @Documentation("outputs the contents of addr for n bytes.")
  @StackEffect("( addr n -- )")
  val TYPE = for {
    addr <- dataStack(pop)
    n <- dataStack(pop)
    data <- memory(fetch(addr, n))
    _ <- output(print(data))
  } yield ()
}