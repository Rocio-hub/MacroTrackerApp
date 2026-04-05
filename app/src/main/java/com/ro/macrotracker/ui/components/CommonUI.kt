package com.ro.macrotracker.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ro.macrotracker.utils.format
import androidx.compose.ui.Modifier

@Composable
fun MacroBadgeSmall(label: String, value: Double, color: Color) {
    Surface(
        color = color.copy(alpha = 0.15f),
        shape = MaterialTheme.shapes.small,
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(label, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = color)
            Text("${value.format()} g", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.ExtraBold)
        }
    }
}