package com.ro.macrotracker.ui

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class DailyPlannerViewModel : ViewModel() {

    private val _plannerSearchQuery = MutableStateFlow("")
    val plannerSearchQuery = _plannerSearchQuery.asStateFlow()

    fun onSearchQueryChange(newQuery: String) {
        _plannerSearchQuery.value = newQuery
    }
}