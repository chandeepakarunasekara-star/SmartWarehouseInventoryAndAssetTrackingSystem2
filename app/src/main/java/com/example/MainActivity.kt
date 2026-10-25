package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.local.AppDatabase
import com.example.data.repository.WarehouseRepository
import com.example.ui.navigation.MainWarehouseLayout
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.WarehouseViewModel
import com.example.ui.viewmodel.WarehouseViewModelFactory

/**
 * Main Activity for Smart Warehouse Inventory and Asset Tracking System.
 * Serves as the single-activity host for Jetpack Compose navigation.
 * Initializes the offline-first Room database instance and binds ViewModel state.
 */
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Enables modern edge-to-edge drawing across system status and navigation bars
        enableEdgeToEdge()

        // Acquire local Room SQLite database instance
        val database = AppDatabase.getInstance(applicationContext)

        // Repository provides abstracted access to Room database and offline SyncQueue
        val repository = WarehouseRepository(database)

        // Factory instantiates WarehouseViewModel with repository dependency injection
        val viewModelFactory = WarehouseViewModelFactory(repository, applicationContext)

        setContent {
            // Apply minimalist UI theme with muted palette and rounded components
            MyApplicationTheme {
                val warehouseViewModel: WarehouseViewModel = viewModel(factory = viewModelFactory)

                // Main warehouse navigation scaffold and screens
                MainWarehouseLayout(viewModel = warehouseViewModel)
            }
        }
    }
}
