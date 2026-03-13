package com.cristmejia2006.cmcontroldegastos.ui.home

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cristmejia2006.cmcontroldegastos.model.Transaction
import com.cristmejia2006.cmcontroldegastos.model.TransactionType
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(viewModel: HomeViewModel) {
    val searchQuery by viewModel.searchQuery.collectAsState()
    val expensesByCategory = viewModel.expensesByCategory
    val groupedTransactions by viewModel.groupedTransactionsReports.collectAsState()
    val allTransactions by viewModel.allFilteredTransactions.collectAsState()
    val totalExpenses = allTransactions.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        Text(
            text = "Análisis Financiero",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Black,
            color = MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.height(16.dp))

        // Barra de Búsqueda
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { viewModel.setSearchQuery(it) },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Buscar gastos o categorías...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            shape = RoundedCornerShape(16.dp),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
            )
        )

        Spacer(modifier = Modifier.height(24.dp))

        if (expensesByCategory.isEmpty() && groupedTransactions.isEmpty()) {
            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                Text("No hay datos para analizar este mes", color = Color.Gray)
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                if (expensesByCategory.isNotEmpty()) {
                    item {
                        Text(
                            "Distribución de Gastos",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(24.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                        ) {
                            Row(
                                modifier = Modifier.padding(24.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                DonutChartReports(
                                    data = expensesByCategory,
                                    modifier = Modifier.size(120.dp)
                                )
                                Spacer(modifier = Modifier.width(32.dp))
                                Column {
                                    expensesByCategory.toList().sortedByDescending { it.second }.forEach { (category, amount) ->
                                        CategoryLegendItemReports(category, amount, totalExpenses)
                                    }
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }

                item {
                    Text(
                        "Historial Detallado",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                // Agrupamos por fecha igual que en el Home, pero para todos los registros del mes
                groupedTransactions.toSortedMap(compareByDescending { it }).forEach { (date, transactions) ->
                    item {
                        DayHeaderReports(date = date, transactions = transactions)
                    }
                    items(transactions) { transaction ->
                        ReportTransactionItem(transaction)
                    }
                }
            }
        }
    }
}

@Composable
fun DayHeaderReports(date: String, transactions: List<Transaction>) {
    val totalIncome = transactions.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
    val totalExpenses = transactions.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
    
    val dateParsed = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(date)
    val displayDate = SimpleDateFormat("dd 'de' MMMM", Locale("es")).format(dateParsed ?: Date())

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        color = Color.Transparent
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = displayDate,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Column(horizontalAlignment = Alignment.End) {
                if (totalIncome > 0) {
                    Text(
                        text = "+$${String.format(Locale.getDefault(), "%.2f", totalIncome)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF2ECC71),
                        fontWeight = FontWeight.Bold
                    )
                }
                if (totalExpenses > 0) {
                    Text(
                        text = "-$${String.format(Locale.getDefault(), "%.2f", totalExpenses)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFFE74C3C),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun ReportTransactionItem(transaction: Transaction) {
    val categoryIcon = when (transaction.category.lowercase()) {
        "comida", "restaurante", "alimento" -> Icons.Default.Restaurant
        "transporte", "uber", "taxi", "bus" -> Icons.Default.DirectionsCar
        "compras", "shopping", "mall" -> Icons.Default.ShoppingBag
        "sueldo", "salario", "pago" -> Icons.Default.Payments
        "ocio", "diversion", "cine" -> Icons.Default.ConfirmationNumber
        "salud", "farmacia", "medico" -> Icons.Default.MedicalServices
        "hogar", "casa", "renta" -> Icons.Default.Home
        else -> Icons.Default.Category
    }
    
    val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    val timeDisplay = timeFormat.format(Date(transaction.date))
    val amountColor = if (transaction.type == TransactionType.INCOME) Color(0xFF2ECC71) else Color(0xFFE74C3C)

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(categoryIcon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(transaction.description, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text(timeDisplay, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }
            
            Text(
                text = if (transaction.type == TransactionType.INCOME) "+$${transaction.amount}" else "-$${transaction.amount}",
                color = amountColor,
                fontWeight = FontWeight.Black,
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

@Composable
fun DonutChartReports(data: Map<String, Double>, modifier: Modifier = Modifier) {
    val total = data.values.sum()
    val colors = listOf(Color(0xFF3498DB), Color(0xFF9B59B6), Color(0xFFF1C40F), Color(0xFFE67E22), Color(0xFF1ABC9C))
    
    Canvas(modifier = modifier) {
        var startAngle = -90f
        data.values.forEachIndexed { index, value ->
            val sweepAngle = (value / total).toFloat() * 360f
            drawArc(
                color = colors[index % colors.size],
                startAngle = startAngle,
                sweepAngle = sweepAngle,
                useCenter = false,
                style = Stroke(width = 30f, cap = StrokeCap.Round)
            )
            startAngle += sweepAngle
        }
    }
}

@Composable
fun CategoryLegendItemReports(category: String, amount: Double, total: Double) {
    val percentage = (amount / total * 100).toInt()
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 4.dp)) {
        Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary))
        Spacer(modifier = Modifier.width(10.dp))
        Text(category, style = MaterialTheme.typography.bodySmall, modifier = Modifier.width(90.dp), maxLines = 1)
        Text("$percentage%", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
    }
}
