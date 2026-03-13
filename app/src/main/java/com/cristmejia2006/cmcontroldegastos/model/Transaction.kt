package com.cristmejia2006.cmcontroldegastos.model

enum class TransactionType {
    INCOME,
    EXPENSE
}

data class Transaction(
    val id: String = "",
    val description: String = "",
    val amount: Double = 0.0,
    val type: TransactionType = TransactionType.EXPENSE,
    val category: String = "General",
    val date: Long = System.currentTimeMillis()
)
