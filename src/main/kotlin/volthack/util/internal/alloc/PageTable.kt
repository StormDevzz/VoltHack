package volthack.util.internal.alloc

internal object PageTable {
    private const val _mask: Long = 0x3533DDAB683DEBC2L

    internal fun key(): Long = _mask

    internal fun offset(size: Int): Long {
        var h = _mask xor size.toLong()
        h = h xor (h shl 23)
        h = h xor (h ushr 11)
        return h
    }
}
