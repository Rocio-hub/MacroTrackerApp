package com.ro.macrotracker.utils

import kotlin.math.roundToInt

    fun Double.format(): String {
        return this.roundToInt().toString()
    }