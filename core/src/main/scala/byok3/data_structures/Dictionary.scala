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

package byok3.data_structures

import byok3.annonation.{Documentation, Immediate, Internal}
import byok3.implicits._
import byok3.primitives._
import byok3.types.{AppState, Dict, Stack, Word}
import cats.data.StateT
import cats.data.StateT._
import cats.implicits._

import scala.reflect.ClassTag
import scala.reflect.runtime.universe._
import scala.util.Try
import scala.collection.MapView

class Dictionary[K, A](private val byKey: Map[K, Stack[Int]], private val byPosn: Vector[A]) {

  def apply(key: K): A = indexOf(key).flatMap(get).getOrElse {
    throw new NoSuchElementException(s"Word $key not found in dictionary")
  }

  def add(key: K, a: A): Dictionary[K, A] =
    new Dictionary(byKey.updated(key, byPosn.length :: byKey.getOrElse(key, Nil)), byPosn :+ a)

  def replace(key: K, a: A): Dictionary[K, A] =
    byKey.get(key) match {
      case Some(idx :: _) => new Dictionary(byKey, byPosn.updated(idx, a))
      case _ => throw new NoSuchElementException(key.toString)
    }

  def forget(key: K): Dictionary[K, A] =
    byKey.get(key) match {
      case Some(_ :: rest) => new Dictionary(byKey.updated(key, rest), byPosn)
      case _ => throw new NoSuchElementException(key.toString)
    }

  def contains(key: K): Boolean =
    byKey.contains(key) && byKey(key).nonEmpty

  def get(key: K): Option[A] =
    indexOf(key).flatMap(get)

  def get(index: Int): Option[A] =
    Try(byPosn(index)).toOption

  def indexOf(key: K): Option[Int] =
    byKey.get(key).flatMap(_.headOption)

  def keys: Iterable[K] = byKey.keys

  def length: Int = byPosn.length

  def toMap: MapView[K, A] =
    byKey.filter(_._2.nonEmpty).mapValues[A] {
      case idx :: _ => get(idx).get
      case Nil => throw new AssertionError("Should never be nil")
    }
}

object Dictionary {

  private def annotation[T](term: TermSymbol)(implicit ev: TypeTag[T]) = {
    val mirror = runtimeMirror(getClass.getClassLoader)
    term.annotations.find(_.tree.tpe =:= typeOf[T]).map { annotation =>
      val cls = annotation.tree.tpe.typeSymbol.asClass
      val classMirror = mirror.reflectClass(cls)
      val constructor = annotation.tree.tpe.decl(termNames.CONSTRUCTOR).asMethod
      val constructorMirror = classMirror.reflectConstructor(constructor)
      val result = annotation.tree.children.tail.collect {
        case Literal(scala.reflect.runtime.universe.Constant(value)) => value
      }
      constructorMirror(result: _*).asInstanceOf[T]
    }
  }

  private def getExecutionTokens[T](obj: T)(implicit ev: TypeTag[T], ev2: ClassTag[T]): Iterable[ExecutionToken] = {

    val instanceMirror = runtimeMirror(getClass.getClassLoader).reflect(obj)
    for {
      decl <- typeOf[T].decls
      term = decl.asTerm
      if term.isVal && term.typeSignature =:= typeOf[AppState[Unit]]
      name = term.toString.substring(6).toUpperCase
      effect = instanceMirror.reflectField(term).get.asInstanceOf[AppState[Unit]]
      documentation = annotation[Documentation](term)
      immediate = annotation[Immediate](term).isDefined
      internal = annotation[Internal](term).isDefined
    } yield {
      Primitive(name, effect, immediate, internal, documentation)
    }
  }

  def apply(): Dict = {
    val tokens =
      getExecutionTokens(Arithmetics) ++
        getExecutionTokens(BitLogic) ++
        getExecutionTokens(Comparison) ++
        getExecutionTokens(Compiler) ++
        getExecutionTokens(DoublePrecisionNumbers) ++
        getExecutionTokens(FlowControl) ++
        getExecutionTokens(IO) ++
        getExecutionTokens(Memory) ++
        getExecutionTokens(StackManipulation) ++
        Seq(
          Constant("TRUE", -1), // TODO: move to system.fth?
          Constant("FALSE", 0))

    val zero: Dict = Dictionary.empty[Word, ExecutionToken]
    tokens.foldLeft(zero) {
      (m, a) => m.add(a.toString, a)
    }
  }

  def empty[K, A]: Dictionary[K, A] = new Dictionary(Map.empty[K, List[Int]], Vector.empty[A])

  def add(exeTok: ExecutionToken): StateT[Try, Dict, Unit] =
    modify[Try, Dict](_.add(exeTok.name, exeTok))

  def replace(exeTok: ExecutionToken): StateT[Try, Dict, Unit] =
    modify[Try, Dict](_.replace(exeTok.name, exeTok))

  def forget(token: Word): StateT[Try, Dict, Unit] =
    modify[Try, Dict](_.forget(token))

  def exists(token: Word): StateT[Try, Dict, Boolean] =
    inspect(_.get(token).isDefined)

  def addressOf(token: Word): StateT[Try, Dict, Int] =
    inspectF[Try, Dict, Int](_.indexOf(token).toTry(Error(-13, token)))

  def instruction(index: Int): StateT[Try, Dict, ExecutionToken] =
    inspectF[Try, Dict, ExecutionToken](_.get(index).toTry(Error(-13)))

  def instruction(token: Word): StateT[Try, Dict, ExecutionToken] =
    inspectF[Try, Dict, ExecutionToken](_.get(token).toTry(Error(-13, token)))

  def last(): StateT[Try, Dict, ExecutionToken] =
    inspectF[Try, Dict, ExecutionToken](dict => dict.get(dict.length - 1).toTry(Error(-13)))
}

