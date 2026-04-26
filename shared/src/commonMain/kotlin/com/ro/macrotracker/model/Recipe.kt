package com.ro.macrotracker.model

data class Recipe(
    val id: Int = 0,
    val name: String,
    val isDeleted: Boolean = false,
    val imageUri: String? = null,
)