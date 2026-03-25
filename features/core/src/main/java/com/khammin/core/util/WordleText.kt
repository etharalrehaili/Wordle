package com.khammin.core.util

import java.text.Normalizer
import java.util.Locale

fun String.normalizeForWordle(): String {
    val nfc = Normalizer.normalize(trim(), Normalizer.Form.NFC).uppercase(Locale.ROOT)
    return buildString(nfc.length) {
        for (ch in nfc) {
            append(
                when (ch) {
                    '\u06CC' -> '\u064A' // Persian / Urdu Yeh → Arabic Yeh (ي)
                    '\u0623', // أ Alef + hamza above
                    '\u0625', // إ Alef + hamza below
                    '\u0622', // آ Alef + madda
                    '\u0671', // ٱ Alef wasla
                        -> '\u0627' // ا plain Alef
                    else -> ch
                }
            )
        }
    }
}
