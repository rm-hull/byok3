package byok3

import cats.{Applicative, FlatMap}
import cats.data.StateT
import cats.data.StateT.pure

package object helpers {

  def sequence[F[_], S, A](sas: StateT[F, S, A]*)(implicit F: Applicative[F], F2: FlatMap[F]): StateT[F, S, List[A]] =
    sas.foldRight(pure[F, S, List[A]](List.empty)) {
      (f, acc) => f.map2(acc)(_ :: _)
    }
}