package QQ

import android.util.Log
import kotlinx.coroutines.delay

class SearchRepository {

    private val items = listOf(
        "Android",
        "Kotlin",
        "ViewBinding",
        "RecyclerView",
        "ViewModel",
        "StateFlow",
        "Coroutines",
        "Gradle Kotlin DSL"
    )

    suspend fun search(query: String): List<String> {
        Log.d("SearchRepository", "Виконано пошук: $query")
        delay(500)

        return items.filter { item ->
            item.contains(query, ignoreCase = true)
        }
    }
}
