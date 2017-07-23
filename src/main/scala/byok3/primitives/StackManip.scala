package byok3.primitives

import byok3.Stack._

object StackManip {

  val drop = pop.map(_ => ())

  val swap = for {
    a <- pop
    b <- pop
    _ <- push(a)
    _ <- push(b)
  } yield ()

  val dup = for {
    a <- pop
    _ <- push(a)
    _ <- push(a)
  } yield ()


}
