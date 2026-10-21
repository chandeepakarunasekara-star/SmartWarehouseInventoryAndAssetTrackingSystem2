package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.TransactionType
import com.example.ui.components.MinimalCard
import com.example.ui.theme.MutedAmber
import com.example.ui.theme.MutedCoral
import com.example.ui.theme.SageTeal
import com.example.ui.theme.SageTealContainer
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate600
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate900
import com.example.ui.viewmodel.WarehouseViewModel

/**
 * Analytics and Inventory Reports screen.
 * Displays financial inventory valuation, category distribution,
 * turnover frequency, and stock reliability health.
 */
@Composable
fun ReportsScreen(
    viewModel: WarehouseViewModel,
    modifier: Modifier = Modifier
) {
    val products by viewModel.allProducts.collectAsState()
    val transactions by viewModel.transactions.collectAsState()
    val categories by viewModel.categories.collectAsState()

    val totalValuation = products.sumOf { it.unitPrice * it.quantity }
    val totalUnits = products.sumOf { it.quantity }
    val lowStockCount = products.count { it.quantity in 1..it.minimumStockLevel }
    val outOfStockCount = products.count { it.quantity == 0 }
    val healthyCount = products.count { it.quantity > it.minimumStockLevel }

    val stockInCount = transactions.count { it.transactionType == TransactionType.STOCK_IN }
    val stockOutCount = transactions.count { it.transactionType == TransactionType.STOCK_OUT }
    val transferCount = transactions.count { it.transactionType == TransactionType.STOCK_TRANSFER }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("reports_screen")
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Financial Valuation Summary Card
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = SageTealContainer),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Total Inventory Valuation",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = Slate700
                        )
                        Icon(
                            imageVector = Icons.Default.MonetizationOn,
                            contentDescription = null,
                            tint = SageTeal,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Text(
                        text = "$${String.format(java.util.Locale.US, "%,.2f", totalValuation)}",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = Slate900
                    )

                    Text(
                        text = "$totalUnits physical items across ${products.size} active catalog SKUs",
                        style = MaterialTheme.typography.bodySmall,
                        color = Slate600
                    )
                }
            }
        }

        // 2. Stock Health Distribution
        item {
            MinimalCard {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Stock Health Breakdown",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = Slate900
                    )

                    HealthMetricRow("In Stock / Healthy", healthyCount, products.size, SageTeal)
                    HealthMetricRow("Low Stock Warning", lowStockCount, products.size, MutedAmber)
                    HealthMetricRow("Out of Stock (Zero)", outOfStockCount, products.size, MutedCoral)
                }
            }
        }

        // 3. Category Distribution
        item {
            MinimalCard {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Category,
                            contentDescription = null,
                            tint = SageTeal,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "Category Inventory Volume",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = Slate900
                        )
                    }

                    categories.forEach { cat ->
                        val catProducts = products.filter { it.categoryId == cat.categoryId }
                        val catUnits = catProducts.sumOf { it.quantity }
                        val catValuation = catProducts.sumOf { it.unitPrice * it.quantity }
                        val ratio = if (totalUnits > 0) catUnits.toFloat() / totalUnits.toFloat() else 0f

                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = cat.name,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = "$catUnits units ($${String.format(java.util.Locale.US, "%,.0f", catValuation)})",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Slate600
                                )
                            }
                            LinearProgressIndicator(
                                progress = { ratio },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = SageTeal,
                                trackColor = Slate200
                            )
                        }
                    }
                }
            }
        }

        // 4. Operational Turnover Activity
        item {
            MinimalCard {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Warehouse Activity Ledger Summary",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = Slate900
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        ActivityStatBox("Stock In Receipts", "$stockInCount", SageTeal, Modifier.weight(1f))
                        Spacer(modifier = Modifier.size(8.dp))
                        ActivityStatBox("Stock Out Dispatches", "$stockOutCount", MutedCoral, Modifier.weight(1f))
                        Spacer(modifier = Modifier.size(8.dp))
                        ActivityStatBox("Transfers", "$transferCount", Slate700, Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
private fun HealthMetricRow(
    label: String,
    count: Int,
    total: Int,
    color: androidx.compose.ui.graphics.Color
) {
    val ratio = if (total > 0) count.toFloat() / total.toFloat() else 0f
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = label, style = MaterialTheme.typography.bodySmall, color = Slate700)
            Text(
                text = "$count SKUs (${(ratio * 100).toInt()}%)",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
        LinearProgressIndicator(
            progress = { ratio },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = color,
            trackColor = Slate200
        )
    }
}

@Composable
private fun ActivityStatBox(
    title: String,
    value: String,
    color: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Slate200.copy(alpha = 0.5f))
            .padding(10.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
            Text(text = value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = color)
            Text(text = title, style = MaterialTheme.typography.labelSmall, color = Slate600, fontSize = 10.sp, maxLines = 1)
        }
    }
}
