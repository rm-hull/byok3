package byok3

import cats.data.State
import cats.data.State.pure

package object helpers {

  def sequence[S, A](sas: State[S, A]*): State[S, List[A]] =
    sas.foldRight(pure[S, List[A]](List.empty)) {
      (f, acc) => f.map2(acc)(_ :: _)
    }
}
