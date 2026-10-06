package com.levelup.app.ui.screens.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.levelup.app.data.repository.LevelUpRepository
import com.levelup.app.domain.model.StatAttribute
import com.levelup.app.domain.usecase.ComputeStatsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class StatsUiState(
    val stats: List<StatAttribute> = emptyList(),
    val isLoading: Boolean = true
)

@HiltViewModel
class StatsViewModel @Inject constructor(
    private val repository: LevelUpRepository,
    private val computeStats: ComputeStatsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(StatsUiState())
    val uiState: StateFlow<StatsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.observeUserProgress().collect {
                val attrs = computeStats()
                _uiState.value = StatsUiState(stats = attrs, isLoading = false)
            }
        }
    }
}
