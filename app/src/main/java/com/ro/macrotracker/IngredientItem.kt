package com.ro.macrotracker

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ro.macrotracker.data.local.entity.Ingredient

@Composable
fun IngredientItem(
    ingredient: Ingredient,
    onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        onClick = onClick
    ) {
        Column(modifier = Modifier.padding(12.dp)) {

            Text(text = ingredient.name)

            Text(text = "Calories: ${ingredient.caloriesPer100g} kcal")

            Text(
                text = "Protein: ${ingredient.proteinPer100g}g | " +
                        "Carbs: ${ingredient.carbsPer100g}g | " +
                        "Fat: ${ingredient.fatPer100g}g"
            )
        }
    }
}