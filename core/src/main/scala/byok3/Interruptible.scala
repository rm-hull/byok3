package byok3

import java.util.concurrent.atomic.AtomicBoolean

import sun.misc.Signal

object Interruptible {

  private val interrupted = new AtomicBoolean(false)

  def trigger(block: () => Unit = () => {}) = Signal.handle(new Signal("INT"), _ => {
    block()
    interrupted.set(true)
  })

  def isInterrupted = interrupted.compareAndSet(true, false)
}

