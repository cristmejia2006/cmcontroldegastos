# CM Control de Gastos

**CM Control de Gastos** es una solución móvil avanzada diseñada para el seguimiento y análisis de finanzas personales. Con una interfaz moderna basada en principios de diseño Fintech, esta aplicación permite a los usuarios gestionar su capital con precisión, claridad y seguridad.

---

## Arquitectura y Estructura del Código

El proyecto implementa el patrón de diseño **MVVM (Model-View-ViewModel)** y se fundamenta en los principios de **Clean Architecture**, lo que garantiza una base de código robusta, escalable y mantenible.

### Organización del Proyecto
```text
com.cristmejia2006.cmcontroldegastos/
├── data/
│   └── repository/       # Lógica de persistencia y servicios externos (Firebase)
├── model/                # Modelos de datos inmutables
├── ui/
│   ├── home/             # Componentes de UI: Dashboard, Reportes y Análisis
│   ├── login/            # Flujo de autenticación y registro
│   └── theme/            # Configuración de diseño Material Design 3
└── MainActivity.kt       # Controlador de navegación principal
```

---

## Implementación Técnica

### 1. Modelo de Datos (Capa Model)
Representación de transacciones financieras mediante clases de datos inmutables en Kotlin.
```kotlin
data class Transaction(
    val id: String = "",
    val description: String = "",
    val amount: Double = 0.0,
    val type: TransactionType = TransactionType.EXPENSE,
    val category: String = "General",
    val date: Long = System.currentTimeMillis()
)
```

### 2. Capa de Repositorio (Capa Data)
Integración con **Firebase Cloud Firestore** para sincronización en tiempo real y soporte offline.
```kotlin
suspend fun addTransaction(transaction: Transaction): Result<Unit> {
    val uid = userId ?: return Result.failure(Exception("Not authenticated"))
    return try {
        val docRef = firestore.collection("users").document(uid)
            .collection("transactions").document()
        docRef.set(transaction.copy(id = docRef.id))
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }
}
```

### 3. Gestión de Estado (Capa UI - ViewModel)
Uso de **StateFlow** y **Coroutines** para la gestión reactiva del estado de la interfaz.
```kotlin
val expensesByCategory: Map<String, Double>
    get() = allFilteredTransactions.value
        .filter { it.type == TransactionType.EXPENSE }
        .groupBy { it.category }
        .mapValues { entry -> entry.value.sumOf { it.amount } }
```

---

## Funcionalidades Destacadas

### Gestión Financiera
*   **Balance en Tiempo Real:** Visualización consolidada del capital disponible con una estética profesional.
*   **Resumen de Flujos:** Monitorización precisa de ingresos y egresos mensuales.
*   **Categorización Inteligente:** Clasificación de movimientos asistida por iconografía descriptiva.

### Organización Temporal
*   **Historial Cronológico:** Transacciones agrupadas automáticamente por periodos diarios.
*   **Trazabilidad de Precisión:** Registro exacto de fecha y hora para cada operación.
*   **Filtros Avanzados:** Consultas históricas segmentadas por mes y año.

### Análisis de Datos
*   **Visualización Estadística:** Gráficos de donut dinámicos para el desglose de consumos.
*   **Métricas de Presupuesto:** Cálculo porcentual del impacto por categoría de gasto.
*   **Motor de Búsqueda:** Filtrado instantáneo por descriptores semánticos o categorías.

---

## Configuración del Proyecto

### Requisitos de Software
*   Android Studio Ladybug (2024.2.1) o versión superior.
*   Kotlin 2.0+ y Java Development Kit (JDK) 17.

### Pasos de Instalación
1.  **Clonación del Repositorio:**
    ```bash
    git clone https://github.com/tu-usuario/cmcontroldegastos.git
    ```
2.  **Configuración de Firebase:** Vincular el archivo `google-services.json` en el directorio `app/`.
3.  **Autenticación:** Habilitar el proveedor "Email/Password" en el panel de Firebase.
4.  **Servicio de Datos:** Inicializar Cloud Firestore en la consola de administración.

---

## Distribución y Despliegue

Para la generación de artefactos destinados a Google Play Store:
1.  Acceder a `Build > Generate Signed Bundle / APK`.
2.  Seleccionar el formato **Android App Bundle (.aab)**.
3.  Firmar con la clave privada (.jks) y ejecutar la variante de compilación `release`.

---

## Información del Desarrollador
**Cristian Mejia** - *Ingeniería de Software y Diseño de Interfaces de Usuario*
