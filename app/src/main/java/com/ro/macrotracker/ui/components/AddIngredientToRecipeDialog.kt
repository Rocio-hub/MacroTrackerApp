package com.ro.macrotracker.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ro.macrotracker.model.Ingredient
import androidx.compose.foundation.lazy.items

@Composable
fun AddIngredientToRecipeDialog(
    ingredients: List<Ingredient>,
    onAdd: (Int, Double) -> Unit,
    onDismiss: () -> Unit
) {

    var selectedIngredientId by remember { mutableStateOf<Int?>(null) }
    var grams by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = {
                    onAdd(selectedIngredientId!!, grams.toDoubleOrNull() ?: 0.0)
                },
                enabled = selectedIngredientId != null && (grams.toDoubleOrNull() ?: 0.0) > 0
            ) {
                Text("Add")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Cancel")
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 300.dp)
            ) {

                LazyColumn {
                    items(ingredients) {
                        val isSelected = selectedIngredientId == it.id

                        Text(
                            text = it.name,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedIngredientId = it.id }
                                .padding(8.dp),
                            color = if (isSelected)
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                OutlinedTextField(
                    value = grams,
                    onValueChange = { grams = it },
                    label = { Text("Grams") }
                )

                if (grams.isNotBlank() && grams.toDoubleOrNull() == null) {
                    Text(
                        text = "Enter a valid number",
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    )
}