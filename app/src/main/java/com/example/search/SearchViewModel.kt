package com.example.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class SearchViewModel : ViewModel() {

    private val repository = SearchRepository()
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    val searchResults = searchQuery
        .debounce(400)
        .filter { query -> query.trim().length >= 3 }
        .distinctUntilChanged()
        .map { query ->
            _isLoading.value = true
            try {
                repository.search(query.trim())
            } finally {
                _isLoading.value = false
            }
        }
        .flowOn(Dispatchers.IO)
        .catch {
            _isLoading.value = false
            emit(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val uiResults = combine(searchQuery, searchResults) { query, results ->
        if (query.trim().length < 3) emptyList() else results
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun onQueryChanged(query: String) {
        _searchQuery.value = query
    }
}
