package volthack.util.misc

import kotlin.random.Random

object MiscUtils {
    fun formatTime(millis: Long): String {
        val totalSec = millis / 1000
        val h = totalSec / 3600; val m = (totalSec % 3600) / 60; val s = totalSec % 60
        return if (h > 0) "%d:%02d:%02d".format(h, m, s)
        else "%d:%02d".format(m, s)
    }

    fun formatDecimal(value: Double, decimals: Int = 1): String {
        return "%.${decimals}f".format(value)
    }

    fun formatPercent(value: Double): String {
        return "${(value * 100).toInt()}%"
    }

    fun clamp(value: Float, min: Float, max: Float): Float = value.coerceIn(min, max)
    fun clamp(value: Double, min: Double, max: Double): Double = value.coerceIn(min, max)
    fun clamp(value: Int, min: Int, max: Int): Int = value.coerceIn(min, max)

    fun lerp(a: Float, b: Float, t: Float): Float = a + (b - a) * t
    fun lerp(a: Double, b: Double, t: Double): Double = a + (b - a) * t

    fun inverseLerp(a: Float, b: Float, value: Float): Float {
        return if (b - a == 0f) 0f else (value - a) / (b - a)
    }

    fun mapRange(value: Float, fromMin: Float, fromMax: Float, toMin: Float, toMax: Float): Float {
        return toMin + (value - fromMin) * (toMax - toMin) / (fromMax - fromMin)
    }

    fun ping(): Int {
        val player = net.minecraft.client.Minecraft.getInstance().player ?: return -1
        return player.connection?.getPlayerInfo(player.uuid)?.latency ?: -1
    }

    fun fps(): Int = net.minecraft.client.Minecraft.getInstance().fps

    fun randomInt(min: Int, max: Int): Int = Random.nextInt(min, max + 1)
    fun randomFloat(min: Float, max: Float): Float = Random.nextFloat() * (max - min) + min
    fun randomDouble(min: Double, max: Double): Double = Random.nextDouble() * (max - min) + min

    fun randomString(length: Int): String {
        val chars = "abcdefghijklmnopqrstuvwxyz0123456789"
        return (1..length).map { chars.random() }.joinToString("")
    }

    fun isInteger(str: String): Boolean = str.toIntOrNull() != null
    fun isFloat(str: String): Boolean = str.toFloatOrNull() != null
    fun isNumber(str: String): Boolean = isInteger(str) || isFloat(str)
}