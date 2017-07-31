package byok3.data_structures

import byok3.data_structures.Context._
import byok3.data_structures.Stack._
import byok3.primitives.{Arithmetics, StackManip, Memory => Mem}
import byok3.types.{Address, AppState, Data, Dictionary, Word}
import cats.data.StateT
import cats.data.StateT._
import cats.implicits._

import scala.reflect.ClassTag
import scala.reflect.runtime.{universe => ru}
import scala.util.Try

sealed trait ExecutionToken {
  val name: Word
  val effect: AppState[Unit]
}

case class Primitive(name: Word, effect: AppState[Unit]) extends ExecutionToken

case class Constant(name: Word, value: Data) extends ExecutionToken {
  override val effect = dataStack(push(value))
}

case class Variable(name: Word, addr: Address) extends ExecutionToken {
  override val effect = dataStack(push(addr))
}

object Dictionary {

  private def getExecutionTokens[T](obj: T)(implicit ev: ru.TypeTag[T], ev2: ClassTag[T]): Iterable[ExecutionToken] = {

    val instanceMirror = ru.runtimeMirror(getClass.getClassLoader).reflect(obj)
    for {
      decl <- ru.typeOf[T].decls
      term = decl.asTerm
      if term.isVal && term.typeSignature =:= ru.typeOf[AppState[Unit]]
      name = term.toString.substring(6).toUpperCase
      effect = instanceMirror.reflectField(term).get.asInstanceOf[AppState[Unit]]
    } yield {
      Primitive(name, effect)
    }
  }

  def apply(): Dictionary = {
    val tokens = getExecutionTokens(Arithmetics) ++
      getExecutionTokens(Mem) ++
      getExecutionTokens(StackManip)

    tokens.foldLeft[Dictionary](Map.empty)((m, a) => m + (a.name -> a))
  }

  def add(exeTok: ExecutionToken): StateT[Try, Dictionary, Unit] =
    modify[Try, Dictionary](_.updated(exeTok.name, exeTok))
}
