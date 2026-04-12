package com.ro.macrotracker.utils

import java.util.Locale

    fun Double.format(decimals: Int = 1): String {
        if (this == 0.0) return "0"
        val format = "%.${decimals}f".format(Locale.ENGLISH, this)
        return format.replace(".0", "").replace(",0", "")
    }