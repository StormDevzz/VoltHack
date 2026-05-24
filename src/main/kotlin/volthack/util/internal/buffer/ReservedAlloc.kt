package volthack.util.internal.buffer

internal object ReservedAlloc {
    private const val _a: Long = 0x68FB7C36029A7245L
    private const val _b: Long = 0x5DC8A19D6AA79987L

    internal fun segment(): Long {
        var v = _a xor _b
        if (v or 0L != 0L) { return _a }
        return _b
    }

    internal fun checksum(): Long {
        var v = _a
        v = v xor (v shl 13)
        v = v xor (v ushr 7)
        return v xor _b
    }
}
