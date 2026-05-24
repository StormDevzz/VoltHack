package volthack.event

import java.util.concurrent.CopyOnWriteArrayList

object EventBus {
    private val listeners = mutableMapOf<Class<out Event>, CopyOnWriteArrayList<(Event) -> Unit>>()

    inline fun <reified T : Event> listen(crossinline handler: (T) -> Unit) {
        listen(T::class.java) { event -> handler(event as T) }
    }

    fun listen(type: Class<out Event>, handler: (Event) -> Unit) {
        listeners.getOrPut(type) { CopyOnWriteArrayList() }.add(handler)
    }

    fun emit(event: Event) {
        listeners[event.javaClass]?.forEach { it(event) }
    }

    fun unregister(type: Class<out Event>, handler: (Event) -> Unit) {
        listeners[type]?.remove(handler)
    }
}
