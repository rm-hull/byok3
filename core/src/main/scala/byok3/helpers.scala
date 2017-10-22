/*
 * Copyright (c) 2017 Richard Hull
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package byok3

import java.io.{ByteArrayOutputStream, PipedInputStream, PipedOutputStream}

import cats.data.StateT
import cats.data.StateT.pure
import cats.{Applicative, FlatMap}

import scala.concurrent.{ExecutionContext, Future}
import scala.io.Source
import scala.language.{higherKinds, reflectiveCalls}

package object helpers {

  def sequence[F[_], S, A](sas: StateT[F, S, A]*)(implicit F: Applicative[F], F2: FlatMap[F]): StateT[F, S, List[A]] =
    sas.foldRight(pure[F, S, List[A]](List.empty)) {
      (f, acc) => f.map2(acc)(_ :: _)
    }

  def capturingOutput[A](block: => A): String = {
    using(new ByteArrayOutputStream) { baos =>
      Console.withOut(baos) {
        block
      }
      baos.toString
    }
  }

  def streamOutput[A](block: => A)(implicit ec: ExecutionContext): Stream[String] = {
    val pis = new PipedInputStream
    val pos = new PipedOutputStream(pis)

    Future {
      using(pos) { _ =>
        Console.withOut(pos) {
          block
        }
      }
    }

    val br = Source.fromInputStream(pis).bufferedReader()
    Stream.continually(br.readLine()).takeWhile(_ != null)
  }

  def using[A, T <: {def close()}](resource: T)(block: T => A) = {
    try {
      block(resource)
    } finally {
      if (resource != null) resource.close()
    }
  }
}
