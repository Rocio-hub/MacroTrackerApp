package com.ro.macrotracker.ui.theme

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ro.macrotracker.data.local.entity.Recipe

@Composable
fun AddRecipeScreen(
    modifier: Modifier = Modifier,
    onSave: (Recipe) -> Unit
) {
    var name by remember { mutableStateOf("") }

    Column(modifier = modifier.padding(16.dp)) {

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Recipe name") },
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = {
                val recipe = Recipe(name = name)
                onSave(recipe)
            },
            modifier = Modifier.padding(top = 16.dp)
        ) {
            Text("Save Recipe")
        }
    }
}