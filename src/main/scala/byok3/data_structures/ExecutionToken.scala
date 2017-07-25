package byok3.data_structures

import byok3.primitives.{Arithmetics, StackManip, Memory => Mem}
import byok3.types.{Dictionary, Word}
import cats.data.State

sealed trait ExecutionToken {
  val name: Word
  val effect: State[Context, Unit]
}

case class Primitive(name: Word, effect: State[Context, Unit]) extends ExecutionToken

trait PrimitiveImpl {
  import scala.reflect.runtime.{universe => ru}
  def instanceMirror: ru.InstanceMirror = ru.runtimeMirror(getClass.getClassLoader).reflect(this)
  def typeOf: ru.Type
}

object DictionaryBuilder {

  private def getExecutionTokens(obj: PrimitiveImpl) = {
    for {
      decl <- obj.typeOf.decls
      term = decl.asTerm
      if term.isVal
      name = term.toString.substring(6).toUpperCase
      effect = obj.instanceMirror.reflectField(term).get.asInstanceOf[State[Context, Unit]]
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
