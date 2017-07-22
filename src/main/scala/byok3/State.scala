package byok3


case class State[S, +A](run: S => (A, S)) {

  import byok3.State._

  def map[B](f: A => B): State[S, B] =
    flatMap(a => unit(f(a)))

  def flatMap[B](f: A => State[S, B]): State[S, B] = {
    State(state => {
      val (a, next) = run(state)
      f(a).run(next)
    })
  }

  def map2[B, C](other: State[S, B])(f: (A, B) => C): State[S, C] =
    for {
      a <- this
      b <- other
    } yield f(a, b)
}

object State {

  def sequence[S, A](fs: List[State[S, A]]): State[S, List[A]] =
    fs.foldRight(unit[S, List[A]](List.empty)) {
      (f, acc) => f.map2(acc)(_ :: _)
    }

  def unit[S, A](a: A): State[S, A] =
    State(state => (a, state))

  def modify[S](f: S => S): State[S, Unit] = for {
    s <- get
    _ <- set(f(s))
  } yield ()

  def get[S]: State[S, S] = State(s => (s, s))

  def set[S](s: S): State[S, Unit] = State(_ => ((), s))
}