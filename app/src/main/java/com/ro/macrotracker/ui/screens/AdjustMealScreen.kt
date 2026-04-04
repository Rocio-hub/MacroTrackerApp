package com.ro.macrotracker.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.ro.macrotracker.model.Ingredient
import com.ro.macrotracker.model.Recipe
import androidx.compose.foundation.lazy.items

@Composable
fun AdjustMealScreen(
    recipe: Recipe,
    initialIngredients: List<Pair<Ingredient, Double>>,
    onConfirm: (List<Pair<Ingredient, Double>>) -> Unit,
    onCancel: () -> Unit
) {
    val editableItems = remember {
        mutableStateListOf<Pair<Ingredient, Double>>().apply { addAll(initialIngredients) }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(
            text = "Adjusting: ${recipe.name}",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Changes here won't affect the main recipe.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.secondary
        )

        LazyColumn(modifier = Modifier.weight(1f).padding(vertical = 16.dp)) {
            items(editableItems) { item ->
                val (ing, amount) = item
                Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(ing.name, modifier = Modifier.weight(1f))

                        OutlinedTextField(
                            value = amount.toString(),
                            onValueChange = { newValue ->
                                val newAmount = newValue.toDoubleOrNull() ?: 0.0
                                val index = editableItems.indexOf(item)
                                if (index != -1) editableItems[index] = ing to newAmount
                            },
                            modifier = Modifier.width(90.dp),
                            label = { Text(ing.unit) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true
                        )
                    }
                }
            }
        }

        Button(
            onClick = { onConfirm(editableItems.toList()) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Confirm Meal for Today")
        }

        TextButton(onClick = onCancel, modifier = Modifier.fillMaxWidth()) {
            Text("Cancel")
        }
    }
}