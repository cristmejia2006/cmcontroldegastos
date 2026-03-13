package com.cristmejia2006.cmcontroldegastos.data.repository

import com.cristmejia2006.cmcontroldegastos.model.Transaction
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class TransactionRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) {
    private val userId: String?
        get() = auth.currentUser?.uid

    fun getTransactions(): Flow<List<Transaction>> = callbackFlow {
        val uid = userId ?: return@callbackFlow
        val subscription = firestore.collection("users").document(uid)
            .collection("transactions")
            .orderBy("date", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val transactions = snapshot?.toObjects(Transaction::class.java) ?: emptyList()
                trySend(transactions)
            }
        awaitClose { subscription.remove() }
    }

    suspend fun addTransaction(transaction: Transaction): Result<Unit> {
        val uid = userId ?: return Result.failure(Exception("Usuario no autenticado"))
        return try {
            val docRef = firestore.collection("users").document(uid)
                .collection("transactions").document()
            val transactionWithId = transaction.copy(id = docRef.id)
            docRef.set(transactionWithId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteTransaction(transactionId: String): Result<Unit> {
        val uid = userId ?: return Result.failure(Exception("Usuario no autenticado"))
        return try {
            firestore.collection("users").document(uid)
                .collection("transactions").document(transactionId)
                .delete()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateTransaction(transaction: Transaction): Result<Unit> {
        val uid = userId ?: return Result.failure(Exception("Usuario no autenticado"))
        return try {
            firestore.collection("users").document(uid)
                .collection("transactions").document(transaction.id)
                .set(transaction)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
