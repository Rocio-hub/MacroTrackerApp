package com.ro.macrotracker.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ro.macrotracker.model.Ingredient
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.ui.Alignment

@Composable
fun AddIngredientToRecipeDialog(
    ingredients: List<Ingredient>,
    onIngredientsSelected: (List<Ingredient>) -> Unit,
    onNewIngredientClick: () -> Unit,
    onDismiss: () -> Unit
) {

    val selectedIds = remember { mutableStateListOf<Int>() }
    var searchQuery by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Ingredients") },
        text = {
            Column(modifier = Modifier.heightIn(max = 400.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Select Ingredients", style = MaterialTheme.typography.titleMedium)

                    TextButton(onClick = onNewIngredientClick) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(Modifier.width(4.dp))
                        Text("New Ingredient")
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search...") },
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                    singleLine = true
                )

                LazyColumn {
                    val filtered = ingredients.filter { it.name.contains(searchQuery, ignoreCase = true) }
                    items(filtered) { ingredient ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    if (selectedIds.contains(ingredient.id)) selectedIds.remove(ingredient.id)
                                    else selectedIds.add(ingredient.id)
                                }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = selectedIds.contains(ingredient.id),
                                onCheckedChange = null
                            )
                            Text(ingredient.name, modifier = Modifier.padding(start = 8.dp))
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val ingredientsToAdd = ingredients.filter { selectedIds.contains(it.id) }
                    onIngredientsSelected(ingredientsToAdd)
                },
                enabled = selectedIds.isNotEmpty()
            ) {
                Text("Add Selected (${selectedIds.size})")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}