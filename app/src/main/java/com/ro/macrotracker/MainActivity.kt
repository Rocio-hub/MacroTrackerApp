package com.ro.macrotracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.ro.macrotracker.ui.theme.MacroTrackerTheme
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import kotlinx.coroutines.launch
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import com.ro.macrotracker.data.local.entity.Ingredient

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val db = androidx.room.Room.databaseBuilder(
            applicationContext,
            com.ro.macrotracker.data.local.database.Database::class.java,
            "macro-db"
        ).build()

        val dao = db.ingredientDao()

        setContent {
            val ingredients by dao.getAllIngredients().collectAsState(initial = emptyList())
            var showAddScreen by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }
            val scope = androidx.compose.runtime.rememberCoroutineScope()
            var selectedIngredient by remember { mutableStateOf<Ingredient?>(null) }

            MacroTrackerTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    floatingActionButton = {
                        androidx.compose.material3.FloatingActionButton(
                            onClick = { showAddScreen = true }
                        ) {
                            Text("+")
                        }
                    }
                ) { innerPadding ->

                    when {
                        selectedIngredient != null -> {
                            IngredientDetailScreen(
                                ingredient = selectedIngredient!!,
                                onBack = { selectedIngredient = null }
                            )
                        }

                        showAddScreen -> {
                            AddIngredientScreen(
                                modifier = Modifier.padding(innerPadding),
                                onSave = { ingredient ->
                                    scope.launch {
                                        dao.insertIngredient(ingredient)
                                        showAddScreen = false
                                    }
                                }
                            )
                        }

                        else -> {
                            LazyColumn(modifier = Modifier.padding(innerPadding)) {
                                items(ingredients) { ingredient ->
                                    IngredientItem(
                                        ingredient = ingredient,
                                        onClick = { selectedIngredient = ingredient }
                                    )
                                }
                            }
                        }
                    }

                }
            }
        }
    }

}
