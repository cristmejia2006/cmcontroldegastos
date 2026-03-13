package com.cristmejia2006.cmcontroldegastos.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.cristmejia2006.cmcontroldegastos.model.Transaction
import com.cristmejia2006.cmcontroldegastos.model.TransactionType
import com.google.firebase.auth.FirebaseAuth
import java.text.DateFormatSymbols
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = viewModel(),
    onAddTransaction: () -> Unit,
    onLogout: () -> Unit,
    isDarkMode: Boolean,
    onThemeChange: (Boolean) -> Unit
) {
    val groupedTransactions by viewModel.recentTransactionsHome.collectAsState()
    val selectedMonth by viewModel.selectedMonth.collectAsState()
    val selectedYear by viewModel.selectedYear.collectAsState()
    
    var showMonthPicker by remember { mutableStateOf(false) }
    var transactionToEdit by remember { mutableStateOf<Transaction?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Column(modifier = Modifier.clickable { showMonthPicker = true }) {
                        Text(
                            "Mi Billetera", 
                            fontWeight = FontWeight.Black, 
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "${getMonthNameLocal(selectedMonth)} $selectedYear",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                            )
                            Icon(
                                Icons.Default.ArrowDropDown, 
                                contentDescription = null, 
                                modifier = Modifier.size(16.dp), 
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                },
                actions = {
                    IconButton(onClick = { onThemeChange(!isDarkMode) }) {
                        Icon(
                            imageVector = if (isDarkMode) Icons.Default.Brightness7 else Icons.Default.Brightness4,
                            contentDescription = "Cambiar Tema",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    IconButton(onClick = {
                        FirebaseAuth.getInstance().signOut()
                        onLogout()
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Cerrar Sesión")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddTransaction,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp),
                elevation = FloatingActionButtonDefaults.elevation(8.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Nuevo Movimiento")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Tarjeta de Saldo Principal - FONDO AZUL PROFUNDO PROFESIONAL
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isDarkMode) Color(0xFF1E293B) else Color(0xFF004AAD)
                )
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text(
                        "BALANCE DISPONIBLE", 
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                    Text(
                        "$${String.format(Locale.getDefault(), "%.2f", viewModel.totalBalance)}",
                        style = MaterialTheme.typography.displayMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    val allTransactionsThisMonth by viewModel.allFilteredTransactions.collectAsState()
                    val totalIncome = allTransactionsThisMonth.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
                    val totalExpenses = allTransactionsThisMonth.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        SummaryItemLocal(
                            label = "Ingresos",
                            amount = totalIncome,
                            icon = Icons.AutoMirrored.Filled.TrendingUp,
                            color = Color(0xFF2ECC71),
                            textColor = Color.White
                        )
                        SummaryItemLocal(
                            label = "Gastos",
                            amount = totalExpenses,
                            icon = Icons.AutoMirrored.Filled.TrendingDown,
                            color = Color(0xFFE74C3C),
                            textColor = Color.White
                        )
                    }
                }
            }

            if (groupedTransactions.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No hay movimientos recientes", color = Color.Gray)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 88.dp)
                ) {
                    groupedTransactions.toSortedMap(compareByDescending { it }).forEach { (date, transactions) ->
                        item {
                            DayHeader(date = date, transactions = transactions)
                        }
                        items(transactions) { transaction ->
                            TransactionItemWithActions(
                                transaction = transaction,
                                onDelete = { viewModel.deleteTransaction(transaction.id) },
                                onEdit = { transactionToEdit = it }
                            )
                        }
                    }
                }
            }
        }

        if (showMonthPicker) {
            MonthYearPickerDialogLocal(
                currentMonth = selectedMonth,
                currentYear = selectedYear,
                onDismiss = { showMonthPicker = false },
                onConfirm = { month, year ->
                    viewModel.setMonth(month)
                    viewModel.setYear(year)
                    showMonthPicker = false
                }
            )
        }

        transactionToEdit?.let { transaction ->
            EditTransactionDialog(
                transaction = transaction,
                onDismiss = { transactionToEdit = null },
                onConfirm = { updated ->
                    viewModel.updateTransaction(updated)
                    transactionToEdit = null
                }
            )
        }
    }
}

@Composable
fun DayHeader(date: String, transactions: List<Transaction>) {
    val totalSpent = transactions.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = formatDisplayDate(date),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = Color.Gray
        )
        if (totalSpent > 0) {
            Text(
                text = "Gastado: -$${String.format(Locale.getDefault(), "%.2f", totalSpent)}",
                style = MaterialTheme.typography.labelSmall,
                color = Color.Gray
            )
        }
    }
}

fun formatDisplayDate(dateStr: String): String {
    val fmt = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val date = fmt.parse(dateStr) ?: return dateStr
    val now = Calendar.getInstance()
    val cal = Calendar.getInstance().apply { time = date }
    
    return when {
        now.get(Calendar.YEAR) == cal.get(Calendar.YEAR) && 
        now.get(Calendar.DAY_OF_YEAR) == cal.get(Calendar.DAY_OF_YEAR) -> "Hoy"
        
        now.get(Calendar.YEAR) == cal.get(Calendar.YEAR) && 
        now.get(Calendar.DAY_OF_YEAR) - 1 == cal.get(Calendar.DAY_OF_YEAR) -> "Ayer"
        
        else -> SimpleDateFormat("dd 'de' MMMM", Locale("es")).format(date)
    }
}

@Composable
fun TransactionItemWithActions(
    transaction: Transaction,
    onDelete: () -> Unit,
    onEdit: (Transaction) -> Unit
) {
    var showOptions by remember { mutableStateOf(false) }

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
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clickable { showOptions = true },
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(categoryIcon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(transaction.description, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.Schedule, null, modifier = Modifier.size(12.dp), tint = Color.Gray)
                    Text(" $timeDisplay  •  ${transaction.category}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                }
            }
            
            Text(
                text = if (transaction.type == TransactionType.INCOME) "+$${transaction.amount}" else "-$${transaction.amount}",
                color = amountColor,
                fontWeight = FontWeight.Black,
                style = MaterialTheme.typography.titleMedium
            )
        }
    }

    if (showOptions) {
        AlertDialog(
            onDismissRequest = { showOptions = false },
            title = { Text("Opciones de Movimiento") },
            text = { Text("¿Qué deseas hacer con '${transaction.description}'?") },
            confirmButton = {
                Row {
                    TextButton(onClick = { 
                        onEdit(transaction)
                        showOptions = false
                    }) {
                        Icon(Icons.Outlined.Edit, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Editar")
                    }
                    TextButton(onClick = { 
                        onDelete()
                        showOptions = false
                    }) {
                        Icon(Icons.Outlined.Delete, null, modifier = Modifier.size(18.dp), tint = Color.Red)
                        Spacer(Modifier.width(4.dp))
                        Text("Eliminar", color = Color.Red)
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { showOptions = false }) { Text("Cancelar") }
            }
        )
    }
}

@Composable
fun EditTransactionDialog(
    transaction: Transaction,
    onDismiss: () -> Unit,
    onConfirm: (Transaction) -> Unit
) {
    var description by remember { mutableStateOf(transaction.description) }
    var amount by remember { mutableStateOf(transaction.amount.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Editar Movimiento") },
        text = {
            Column {
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Descripción") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(16.dp))
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("Monto") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { 
                    val amt = amount.toDoubleOrNull() ?: transaction.amount
                    onConfirm(transaction.copy(description = description, amount = amt))
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Guardar Cambios")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) { Text("Cancelar") }
        }
    )
}

fun getMonthNameLocal(month: Int): String {
    return DateFormatSymbols(Locale("es")).months[month].replaceFirstChar { it.uppercase() }
}

@Composable
fun SummaryItemLocal(label: String, amount: Double, icon: ImageVector, color: Color, textColor: Color = Color.Unspecified) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Surface(shape = CircleShape, color = color.copy(alpha = 0.2f), modifier = Modifier.size(36.dp)) {
            Box(contentAlignment = Alignment.Center) {
                Icon(icon, null, tint = color, modifier = Modifier.size(20.dp))
            }
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(label, style = MaterialTheme.typography.labelSmall, color = if (textColor != Color.Unspecified) textColor.copy(alpha = 0.7f) else Color.Gray)
            Text("$${String.format(Locale.getDefault(), "%.2f", amount)}", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = if (textColor != Color.Unspecified) textColor else Color.Unspecified)
        }
    }
}

@Composable
fun MonthYearPickerDialogLocal(
    currentMonth: Int,
    currentYear: Int,
    onDismiss: () -> Unit,
    onConfirm: (Int, Int) -> Unit
) {
    var month by remember { mutableIntStateOf(currentMonth) }
    var year by remember { mutableIntStateOf(currentYear) }
    val months = DateFormatSymbols(Locale("es")).months

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Seleccionar Periodo", fontWeight = FontWeight.Bold) },
        text = {
            Column {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { year-- }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) }
                    Text(year.toString(), fontWeight = FontWeight.Black, fontSize = 20.sp)
                    IconButton(onClick = { year++ }) { Icon(Icons.Default.ArrowForward, null) }
                }
                Spacer(modifier = Modifier.height(16.dp))
                LazyColumn(modifier = Modifier.height(220.dp)) {
                    items(months.size) { index ->
                        if (months[index].isNotEmpty()) {
                            val isSelected = month == index
                            Surface(
                                modifier = Modifier.fillMaxWidth().clickable { month = index }.padding(vertical = 4.dp),
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent
                            ) {
                                Text(months[index].replaceFirstChar { it.uppercase() }, modifier = Modifier.padding(12.dp), color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = { Button(onClick = { onConfirm(month, year) }) { Text("Confirmar") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cerrar") } }
    )
}
