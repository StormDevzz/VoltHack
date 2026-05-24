package volthack.util.discord.internal.runtime

import volthack.util.internal.alloc.PageTable
import volthack.util.internal.buffer.ReservedAlloc

internal object ClientHandle {
    private const val _encrypted: Long = 0x492501D4366799FDL

    private val decoded: Long by lazy {
        val a = ReservedAlloc.segment()
        val b = PageTable.key()
        val key = a xor b
        _encrypted xor key
    }

    internal val clientId: Long get() = decoded
    internal val clientIdString: String get() = decoded.toString()
}
