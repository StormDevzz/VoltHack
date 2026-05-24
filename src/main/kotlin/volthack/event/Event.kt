package volthack.event

import org.joml.Matrix4f

open class Event {
    var cancelled = false
}

class TickEvent : Event()

class Render3DEvent(
    val partialTicks: Float,
    val modelViewMatrix: Matrix4f,
    val projectionMatrix: Matrix4f
) : Event()
