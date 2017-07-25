package byok3.data_structures

import byok3.primitives.{Arithmetics, StackManip, Memory => Mem}
import byok3.types.{Dictionary, Word}
import cats.data.State

import scala.reflect.ClassTag
import scala.reflect.runtime.{universe => ru}

sealed trait ExecutionToken {
  val name: Word
  val effect: State[Context, Unit]
}

case class Primitive(name: Word, effect: State[Context, Unit]) extends ExecutionToken


object DictionaryBuilder {

  private def getExecutionTokens[T](obj: T)(implicit ev: ru.TypeTag[T], ev2: ClassTag[T]) = {

    val instanceMirror = ru.runtimeMirror(getClass.getClassLoader).reflect(obj)
    for {
      decl <- ru.typeOf[T].decls
      term = decl.asTerm
      if term.isVal && term.typeSignature =:= ru.typeOf[State[Context, Unit]]
      name = term.toString.substring(6).toUpperCase
      effect = instanceMirror.reflectField(term).get.asInstanceOf[State[Context, Unit]]
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
}
