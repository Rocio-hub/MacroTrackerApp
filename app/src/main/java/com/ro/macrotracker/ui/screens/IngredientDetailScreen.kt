package com.ro.macrotracker.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ro.macrotracker.data.local.entity.Ingredient

@Composable
fun IngredientDetailScreen(
    ingredient: Ingredient,
    onBack: () -> Unit
) {
    Column(modifier = Modifier.padding(16.dp)) {

        Text(text = ingredient.name)

        Text(text = "Calories: ${ingredient.caloriesPer100}")
        Text(text = "Protein: ${ingredient.proteinPer100}")
        Text(text = "Carbs: ${ingredient.carbsPer100}")
        Text(text = "Fat: ${ingredient.fatPer100}")

        Button(
            onClick = onBack,
            modifier = Modifier.padding(top = 16.dp)
        ) {
            Text("Back")
        }
    }
}