package com.ro.macrotracker.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ro.macrotracker.model.Ingredient
import com.ro.macrotracker.utils.format

@Composable
fun IngredientItem(
    ingredient: Ingredient,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {

            Column(modifier = Modifier.weight(1f)) {

                Text(text = ingredient.name)

                Text(
                    text = "${ingredient.caloriesPer100.format()} kcal / 100${ingredient.unit}",
                    style = androidx.compose.material3.MaterialTheme.typography.bodyMedium
                )

                Text(
                    text = "Protein: ${ingredient.proteinPer100}g | " +
                            "Carbs: ${ingredient.carbsPer100}g | " +
                            "Fat: ${ingredient.fatPer100}g"
                )
            }

            TextButton(onClick = onDelete) {
                Text("Delete")
            }
        }
    }
}