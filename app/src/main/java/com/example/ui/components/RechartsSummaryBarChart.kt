package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.ProductEntity

enum class ChartMetric {
    QUANTITY,
    VALUATION
}

/**
 * Custom D3 / Recharts-styled interactive Summary Bar Chart built natively in Jetpack Compose.
 *
 * Implements core Recharts features:
 * - CartesianGrid: Subtle horizontal gridlines with Y-axis tick intervals
 * - Bar: Rounded-corner bars with custom gradient shaders
 * - ReferenceLine: Dashed threshold line showing the safety minimum stock level
 * - Tooltip: Interactive hover/touch card with item metadata, status, variance, and valuation
 * - Legend: Clean color-coded indicators for Healthy, Low-Stock Alert, and Out-of-Stock
 */
@Composable
fun RechartsSummaryBarChart(
    products: List<ProductEntity>,
    metric: ChartMetric = ChartMetric.QUANTITY,
    safetyThreshold: Int = 15,
    onProductSelected: (ProductEntity) -> Unit = {},
    modifier: Modifier = Modifier
) {
    if (products.isEmpty()) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(240.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(Color.White)
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No inventory items to display. Add items to see charts.",
                color = Color(0xFF64748B),
                style = MaterialTheme.typography.bodyMedium
            )
        }
        return
    }

    // Limit to top 8-10 items for optimal readable bar spacing
    val displayList = remember(products) { products.take(9) }

    // Selected bar index for interactive Recharts tooltip
    var selectedBarIndex by remember { mutableIntStateOf(-1) }
    val selectedProduct = displayList.getOrNull(selectedBarIndex)

    // Animation progress for smooth entry transitions
    val animationProgress = remember { Animatable(0f) }
    LaunchedEffect(displayList, metric) {
        animationProgress.snapTo(0f)
        animationProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 650, easing = FastOutSlowInEasing)
        )
    }

    // Determine max value for dynamic scaling
    val maxVal = remember(displayList, metric, safetyThreshold) {
        val highest = displayList.maxOfOrNull {
            if (metric == ChartMetric.QUANTITY) it.quantity.toDouble() else (it.quantity * it.unitPrice)
        } ?: 100.0
        val target = if (metric == ChartMetric.QUANTITY) maxOf(highest, safetyThreshold.toDouble() * 1.3) else highest
        if (target <= 0) 100.0 else target * 1.15
    }

    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier
            .fillMaxWidth()
            .testTag("recharts_summary_bar_chart")
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Chart Header & Recharts Indicator Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = if (metric == ChartMetric.QUANTITY) "Stock Levels Summary" else "Stock Valuation Summary",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A)
                    )
                    Text(
                        text = "Interactive Bar Chart • Tap any bar for details",
                        fontSize = 12.sp,
                        color = Color(0xFF64748B)
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFE8F5E9))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "D3 / Recharts Canvas",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF084B34)
                    )
                }
            }

            // Interactive Tooltip Card (Shown when bar is tapped or default selected)
            selectedProduct?.let { prod ->
                RechartsTooltipCard(
                    product = prod,
                    metric = metric,
                    safetyThreshold = safetyThreshold,
                    onDismiss = { selectedBarIndex = -1 }
                )
            }

            // Native Jetpack Compose Canvas Chart
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(230.dp)
                    .pointerInput(displayList) {
                        detectTapGestures { offset ->
                            val leftPadding = 48f
                            val rightPadding = 16f
                            val availableWidth = size.width - leftPadding - rightPadding
                            val barSlotWidth = availableWidth / displayList.size

                            if (offset.x >= leftPadding && offset.x <= size.width - rightPadding) {
                                val clickedIdx = ((offset.x - leftPadding) / barSlotWidth).toInt()
                                if (clickedIdx in displayList.indices) {
                                    selectedBarIndex = if (selectedBarIndex == clickedIdx) -1 else clickedIdx
                                    selectedBarIndex.takeIf { it >= 0 }?.let { onProductSelected(displayList[it]) }
                                }
                            } else {
                                selectedBarIndex = -1
                            }
                        }
                    }
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val width = size.width
                    val height = size.height

                    val leftPadding = 48f
                    val rightPadding = 20f
                    val topPadding = 20f
                    val bottomPadding = 45f

                    val chartWidth = width - leftPadding - rightPadding
                    val chartHeight = height - topPadding - bottomPadding

                    // 1. Draw Cartesian Grid Lines & Y-Axis Ticks
                    val gridLines = 4
                    val textPaint = android.graphics.Paint().apply {
                        color = android.graphics.Color.rgb(148, 163, 184)
                        textSize = 26f
                        isAntiAlias = true
                    }

                    for (i in 0..gridLines) {
                        val ratio = i.toFloat() / gridLines
                        val y = topPadding + chartHeight * (1f - ratio)
                        val tickVal = (maxVal * ratio).toInt()

                        // Grid dashed line
                        drawLine(
                            color = Color(0xFFF1F5F9),
                            start = Offset(leftPadding, y),
                            end = Offset(width - rightPadding, y),
                            strokeWidth = 2f,
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f), 0f)
                        )

                        // Y-axis label text
                        val label = if (metric == ChartMetric.QUANTITY) "$tickVal" else "$${tickVal / 1000}k"
                        drawContext.canvas.nativeCanvas.drawText(
                            label,
                            6f,
                            y + 8f,
                            textPaint
                        )
                    }

                    // 2. Draw Safety Min Reference Line (if viewing by quantity)
                    if (metric == ChartMetric.QUANTITY && safetyThreshold > 0 && maxVal > 0) {
                        val thresholdRatio = (safetyThreshold.toFloat() / maxVal.toFloat()).coerceIn(0f, 1f)
                        val thresholdY = topPadding + chartHeight * (1f - thresholdRatio)

                        drawLine(
                            color = Color(0xFFEF4444),
                            start = Offset(leftPadding, thresholdY),
                            end = Offset(width - rightPadding, thresholdY),
                            strokeWidth = 2.5f,
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 8f), 0f)
                        )

                        val refPaint = android.graphics.Paint().apply {
                            color = android.graphics.Color.rgb(239, 68, 68)
                            textSize = 22f
                            isAntiAlias = true
                            isFakeBoldText = true
                        }
                        drawContext.canvas.nativeCanvas.drawText(
                            "Min: $safetyThreshold",
                            width - rightPadding - 85f,
                            thresholdY - 6f,
                            refPaint
                        )
                    }

                    // 3. Draw Bars for Each Product
                    val barSlotWidth = chartWidth / displayList.size
                    val barWidth = (barSlotWidth * 0.58f).coerceAtLeast(14f)

                    displayList.forEachIndexed { index, product ->
                        val value = if (metric == ChartMetric.QUANTITY) {
                            product.quantity.toDouble()
                        } else {
                            product.quantity * product.unitPrice
                        }

                        val valueRatio = (value / maxVal).coerceIn(0.0, 1.0).toFloat()
                        val animatedBarHeight = chartHeight * valueRatio * animationProgress.value

                        val slotCenterX = leftPadding + (index * barSlotWidth) + (barSlotWidth / 2f)
                        val barLeft = slotCenterX - (barWidth / 2f)
                        val barTop = topPadding + chartHeight - animatedBarHeight

                        // Column active hover/touch highlight (Recharts cursor effect)
                        if (index == selectedBarIndex) {
                            drawRoundRect(
                                color = Color(0xFFF1F5F9),
                                topLeft = Offset(slotCenterX - (barSlotWidth / 2f), topPadding),
                                size = Size(barSlotWidth, chartHeight),
                                cornerRadius = CornerRadius(12f, 12f)
                            )
                        }

                        // Determine gradient colors based on stock health
                        val isLowStock = product.quantity <= product.minimumStockLevel
                        val isOutOfStock = product.quantity == 0

                        val barBrush = when {
                            isOutOfStock -> Brush.verticalGradient(
                                colors = listOf(Color(0xFFEF4444), Color(0xFFDC2626)),
                                startY = barTop,
                                endY = topPadding + chartHeight
                            )
                            isLowStock -> Brush.verticalGradient(
                                colors = listOf(Color(0xFFFBBF24), Color(0xFFD97706)),
                                startY = barTop,
                                endY = topPadding + chartHeight
                            )
                            else -> Brush.verticalGradient(
                                colors = listOf(Color(0xFF4ADE80), Color(0xFF084B34)),
                                startY = barTop,
                                endY = topPadding + chartHeight
                            )
                        }

                        // Draw Bar with rounded top
                        if (animatedBarHeight > 2f) {
                            drawRoundRect(
                                brush = barBrush,
                                topLeft = Offset(barLeft, barTop),
                                size = Size(barWidth, animatedBarHeight),
                                cornerRadius = CornerRadius(10f, 10f)
                            )
                        } else {
                            // Empty baseline marker for 0 stock
                            drawLine(
                                color = Color(0xFFEF4444),
                                start = Offset(barLeft, topPadding + chartHeight - 2f),
                                end = Offset(barLeft + barWidth, topPadding + chartHeight - 2f),
                                strokeWidth = 4f
                            )
                        }

                        // Bar top value indicator
                        if (animatedBarHeight > 20f && animationProgress.value > 0.8f) {
                            val valPaint = android.graphics.Paint().apply {
                                color = if (isOutOfStock) android.graphics.Color.rgb(220, 38, 38)
                                else if (isLowStock) android.graphics.Color.rgb(217, 119, 6)
                                else android.graphics.Color.rgb(8, 75, 52)
                                textSize = 22f
                                textAlign = android.graphics.Paint.Align.CENTER
                                isFakeBoldText = true
                                isAntiAlias = true
                            }
                            val displayNum = if (metric == ChartMetric.QUANTITY) "${product.quantity}" else "$${(value).toInt()}"
                            drawContext.canvas.nativeCanvas.drawText(
                                displayNum,
                                slotCenterX,
                                barTop - 6f,
                                valPaint
                            )
                        }

                        // X-Axis Item Code / SKU label
                        val xLabelPaint = android.graphics.Paint().apply {
                            color = if (index == selectedBarIndex) android.graphics.Color.rgb(15, 23, 42)
                            else android.graphics.Color.rgb(100, 116, 139)
                            textSize = 21f
                            textAlign = android.graphics.Paint.Align.CENTER
                            isFakeBoldText = index == selectedBarIndex
                            isAntiAlias = true
                        }
                        val shortLabel = product.sku.takeLast(4).ifBlank { product.name.take(4) }
                        drawContext.canvas.nativeCanvas.drawText(
                            shortLabel,
                            slotCenterX,
                            height - 12f,
                            xLabelPaint
                        )
                    }

                    // 4. Baseline Axis Line
                    drawLine(
                        color = Color(0xFFCBD5E1),
                        start = Offset(leftPadding, topPadding + chartHeight),
                        end = Offset(width - rightPadding, topPadding + chartHeight),
                        strokeWidth = 2f
                    )
                }
            }

            // Recharts Legend Component
            RechartsChartLegend(metric = metric)
        }
    }
}

/**
 * Recharts Tooltip popup card showing selected product metrics
 */
@Composable
private fun RechartsTooltipCard(
    product: ProductEntity,
    metric: ChartMetric,
    safetyThreshold: Int,
    onDismiss: () -> Unit
) {
    val isLowStock = product.quantity <= product.minimumStockLevel
    val isOutOfStock = product.quantity == 0

    val badgeColor = when {
        isOutOfStock -> Color(0xFFEF4444)
        isLowStock -> Color(0xFFD97706)
        else -> Color(0xFF084B34)
    }

    val badgeBg = when {
        isOutOfStock -> Color(0xFFFEE2E2)
        isLowStock -> Color(0xFFFEF3C7)
        else -> Color(0xFFE8F5E9)
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFFF8FAFC))
            .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(16.dp))
            .padding(14.dp)
            .testTag("recharts_tooltip_card")
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(badgeColor)
                    )
                    Text(
                        text = product.name,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A)
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(badgeBg)
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = when {
                            isOutOfStock -> "OUT OF STOCK"
                            isLowStock -> "LOW STOCK ALERT"
                            else -> "HEALTHY STOCK"
                        },
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = badgeColor
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("SKU: ${product.sku}", fontSize = 12.sp, color = Color(0xFF64748B))
                    Text("Location: ${product.locationId}", fontSize = 12.sp, color = Color(0xFF64748B))
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "${product.quantity} units",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isLowStock) Color(0xFFD97706) else Color(0xFF084B34)
                    )
                    Text(
                        text = "Valuation: $${String.format("%.2f", product.quantity * product.unitPrice)}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF334155)
                    )
                }
            }
        }
    }
}

/**
 * Recharts Legend Component with color swatches
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun RechartsChartLegend(metric: ChartMetric) {
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        LegendItem(color = Color(0xFF084B34), label = "Healthy (> Safety Min)")
        LegendItem(color = Color(0xFFFBBF24), label = "Low-Stock Alert")
        LegendItem(color = Color(0xFFEF4444), label = "Out of Stock")
        if (metric == ChartMetric.QUANTITY) {
            LegendDashedItem(color = Color(0xFFEF4444), label = "Safety Threshold")
        }
    }
}

@Composable
private fun LegendItem(color: Color, label: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(color)
        )
        Text(text = label, fontSize = 11.sp, color = Color(0xFF64748B), fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun LegendDashedItem(color: Color, label: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(
            modifier = Modifier
                .width(16.dp)
                .height(3.dp)
                .background(color)
        )
        Text(text = label, fontSize = 11.sp, color = Color(0xFF64748B), fontWeight = FontWeight.Medium)
    }
}
