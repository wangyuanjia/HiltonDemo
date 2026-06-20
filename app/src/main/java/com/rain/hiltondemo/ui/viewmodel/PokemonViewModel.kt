package com.rain.hiltondemo.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rain.hiltondemo.data.model.Pokemon
import com.rain.hiltondemo.data.model.PokemonSpecies
import com.rain.hiltondemo.data.repository.PokemonRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SearchUiState(
    val query: String = "",
    val species: List<PokemonSpecies> = emptyList(),
    val isLoading: Boolean = false,
    val isLoadMore: Boolean = false,
    val error: String? = null,
    val hasMore: Boolean = false,
    val currentPage: Int = 0
)

class PokemonViewModel : ViewModel() {

    private val repository = PokemonRepository()
    private val pageSize = 10

    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    fun onQueryChange(query: String) {
        _uiState.update { it.copy(query = query) }
    }

    fun search() {
        val query = _uiState.value.query.trim()
        if (query.isBlank()) return

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = true,
                    error = null,
                    species = emptyList(),
                    currentPage = 0,
                    hasMore = false
                )
            }
            repository.searchSpecies(query, limit = pageSize, offset = 0)
                .onSuccess { result ->
                    _uiState.update {
                        it.copy(
                            species = result,
                            isLoading = false,
                            hasMore = result.size == pageSize,
                            currentPage = 0
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(isLoading = false, error = error.message ?: "Unknown error")
                    }
                }
        }
    }

    fun loadMore() {
        val state = _uiState.value
        if (state.isLoadMore || !state.hasMore || state.query.isBlank()) return

        val nextPage = state.currentPage + 1

        viewModelScope.launch {
            _uiState.update { it.copy(isLoadMore = true) }
            repository.searchSpecies(state.query.trim(), limit = pageSize, offset = nextPage * pageSize)
                .onSuccess { result ->
                    _uiState.update {
                        it.copy(
                            species = it.species + result,
                            isLoadMore = false,
                            hasMore = result.size == pageSize,
                            currentPage = nextPage
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(isLoadMore = false, error = error.message ?: "Unknown error")
                    }
                }
        }
    }

    fun getPokemonById(id: Int): Pokemon? {
        return _uiState.value.species
            .flatMap { it.pokemon_v2_pokemons }
            .find { it.id == id }
    }
}
