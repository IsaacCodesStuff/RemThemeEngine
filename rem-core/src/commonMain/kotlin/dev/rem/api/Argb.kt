package dev.rem.api

import kotlin.jvm.JvmInline

/** A 32-bit color encoded as `0xAARRGGBB`. */
@JvmInline
value class Argb private constructor(val value: Int) {
    /** Returns the bit pattern as an unsigned 32-bit value stored in a [Long]. */
    fun toLong(): Long = value.toLong() and 0xFFFF_FFFFL

    companion object {
        /** Wraps any 32-bit ARGB bit pattern. Kotlin `Int` values may be negative. */
        fun fromInt(value: Int): Argb = Argb(value)

        /** Creates an ARGB color from an unsigned 32-bit value in the range `0..0xFFFFFFFF`. */
        fun fromLong(value: Long): Argb {
            require(value in 0L..0xFFFF_FFFFL) {
                "ARGB value must be between 0x00000000 and 0xFFFFFFFF"
            }
            return Argb(value.toInt())
        }
    }
}
