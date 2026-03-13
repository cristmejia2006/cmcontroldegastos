package com.cristmejia2006.cmcontroldegastos.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cristmejia2006.cmcontroldegastos.data.repository.TransactionRepository
import com.cristmejia2006.cmcontroldegastos.model.Transaction
import com.cristmejia2006.cmcontroldegastos.model.TransactionType
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class HomeViewModel(
    private val repository: TransactionRepository = TransactionRepository()
) : ViewModel() {

    private val _selectedMonth = MutableStateFlow(Calendar.getInstance().get(Calendar.MONTH))
    val selectedMonth: StateFlow<Int> = _selectedMonth.asStateFlow()

    private val _selectedYear = MutableStateFlow(Calendar.getInstance().get(Calendar.YEAR))
    val selectedYear: StateFlow<Int> = _selectedYear.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // Todas las transacciones (sin límite) filtradas por mes/año para Reportes
    val allFilteredTransactions: StateFlow<List<Transaction>> = combine(
        repository.getTransactions(),
        _selectedMonth,
        _selectedYear,
        _searchQuery
    ) { transactions, month, year, query ->
        transactions.filter { transaction ->
            val cal = Calendar.getInstance().apply { timeInMillis = transaction.date }
            val matchesDate = cal.get(Calendar.MONTH) == month && cal.get(Calendar.YEAR) == year
            val matchesQuery = transaction.description.contains(query, ignoreCase = true) || 
                               transaction.category.contains(query, ignoreCase = true)
            matchesDate && matchesQuery
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Solo los últimos 100 movimientos para la pantalla de Inicio
    val recentTransactionsHome: StateFlow<Map<String, List<Transaction>>> = repository.getTransactions()
        .map { list ->
            list.take(100) // Límite de 100 para que no se vea "feo" o pesado
                .groupBy { transaction ->
                    val fmt = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    fmt.format(Date(transaction.date))
                }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    // Agrupación para la pestaña de Reportes (todos los del mes)
    val groupedTransactionsReports: StateFlow<Map<String, List<Transaction>>> = allFilteredTransactions
        .map { list ->
            list.groupBy { transaction ->
                val fmt = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                fmt.format(Date(transaction.date))
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    val totalBalance: Double
        get() = allFilteredTransactions.value.sumOf { 
            if (it.type == TransactionType.INCOME) it.amount else -it.amount 
        }

    val expensesByCategory: Map<String, Double>
        get() = allFilteredTransactions.value
            .filter { it.type == TransactionType.EXPENSE }
            .groupBy { it.category }
            .mapValues { entry -> entry.value.sumOf { it.amount } }

    fun setMonth(month: Int) { _selectedMonth.value = month }
    fun setYear(year: Int) { _selectedYear.value = year }
    fun setSearchQuery(query: String) { _searchQuery.value = query }

    fun addTransaction(transaction: Transaction, onComplete: () -> Unit) {
        viewModelScope.launch {
            repository.addTransaction(transaction)
            onComplete()
        }
    }

    fun deleteTransaction(id: String) {
        viewModelScope.launch {
            repository.deleteTransaction(id)
        }
    }

    fun updateTransaction(transaction: Transaction) {
        viewModelScope.launch {
            repository.updateTransaction(transaction)
        }
    }
}
