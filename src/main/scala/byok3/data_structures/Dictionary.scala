package byok3.data_structures

import byok3.annonation.{Documentation, Immediate, Internal}
import byok3.implicits._
import byok3.primitives.{Arithmetics, Compiler, Control, IO, Memory, StackManipulation}
import byok3.types.{AppState, Dict, Word}
import cats.data.StateT
import cats.data.StateT._
import cats.implicits._

import scala.util.Try

class Dictionary[K, A](private val byKey: Map[K, Int], private val byPosn: Vector[A]) {

  def add(key: K, a: A): Dictionary[K, A] =
    new Dictionary(byKey.updated(key, byPosn.length), byPosn :+ a)

  def get(key: K): Option[A] =
    indexOf(key).flatMap(get)

  def get(index: Int): Option[A] =
    Try(byPosn(index)).toOption

  def indexOf(key: K): Option[Int] =
    byKey.get(key)

  def keys = byKey.keys

  def length = byPosn.length

  def toMap = byKey.mapValues(idx => get(idx).get)
}

object Dictionary {

  import scala.reflect.ClassTag
  import scala.reflect.runtime.{universe => ru}

  private def annotation[T](term: ru.TermSymbol)(implicit ev: ru.TypeTag[T]) = {
    val mirror = ru.runtimeMirror(getClass.getClassLoader)
    term.annotations.find(_.tree.tpe =:= ru.typeOf[T]).map { annotation =>
      val cls = annotation.tree.tpe.typeSymbol.asClass
      val classMirror = mirror.reflectClass(cls)
      val constructor = annotation.tree.tpe.decl(ru.termNames.CONSTRUCTOR).asMethod
      val constructorMirror = classMirror.reflectConstructor(constructor)
      val result = annotation.tree.children.tail.collect {
        case ru.Literal(ru.Constant(value)) => value
      }
      constructorMirror(result: _*).asInstanceOf[T]
    }
  }

  private def getExecutionTokens[T](obj: T)(implicit ev: ru.TypeTag[T], ev2: ClassTag[T]): Iterable[ExecutionToken] = {

    val instanceMirror = ru.runtimeMirror(getClass.getClassLoader).reflect(obj)
    for {
      decl <- ru.typeOf[T].decls
      term = decl.asTerm
      if term.isVal && term.typeSignature =:= ru.typeOf[AppState[Unit]]
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
        getExecutionTokens(Control) ++
        getExecutionTokens(Compiler) ++
        getExecutionTokens(IO) ++
        getExecutionTokens(Memory) ++
        getExecutionTokens(StackManipulation)

    tokens.foldLeft[Dict](Dictionary.empty) {
      (m, a) => m.add(a.name, a)
    }
  }

  def empty[K, A]: Dictionary[K, A] = new Dictionary(Map.empty[K, Int], Vector.empty[A])

  def add(exeTok: ExecutionToken): StateT[Try, Dict, Unit] =
    modify[Try, Dict](_.add(exeTok.name, exeTok))

  def addressOf(token: Word): StateT[Try, Dict, Int] =
    inspectF[Try, Dict, Int](_.indexOf(token).toTry(Error(-13, token)))

  def instruction(index: Int): StateT[Try, Dict, ExecutionToken] =
    inspectF[Try, Dict, ExecutionToken](_.get(index).toTry(Error(-13)))

  def last(): StateT[Try, Dict, ExecutionToken] =
    inspectF[Try, Dict, ExecutionToken](dict => dict.get(dict.length - 1).toTry(Error(-13)))
}

