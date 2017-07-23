package byok3.data_structures

import byok3.primitives.{Arithmetics, Memory => Mem, StackManip}
import cats.data.State

sealed trait ExecutionToken {
  val effect: State[Context, Unit]
}

case class Primitive(effect: State[Context, Unit]) extends ExecutionToken

trait PrimitiveImpl {
  import scala.reflect.runtime.{universe => ru}
  def instanceMirror: ru.InstanceMirror = ru.runtimeMirror(getClass.getClassLoader).reflect(this)
  def typeOf: ru.Type
}

object ExecutionTokenMapBuilder {

  private def getExecutionTokens(obj: PrimitiveImpl) = {
    obj.typeOf.decls
      .map(_.asTerm)
      .filter(_.isVal)
      .map(ts => (ts.toString.substring(6).toUpperCase, Primitive(effect = obj.instanceMirror.reflectField(ts).get.asInstanceOf[State[Context, Unit]]))).toMap
  }

  def apply(): Map[String, ExecutionToken] =
    getExecutionTokens(Arithmetics) ++
      getExecutionTokens(Mem) ++
      getExecutionTokens(StackManip)
}
