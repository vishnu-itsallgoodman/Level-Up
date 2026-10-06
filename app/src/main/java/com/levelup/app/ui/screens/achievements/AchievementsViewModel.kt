package com.levelup.app.ui.screens.achievements

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.levelup.app.data.database.AchievementEntity
import com.levelup.app.data.repository.LevelUpRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@HiltViewModel
class AchievementsViewModel @Inject constructor(
    repository: LevelUpRepository
) : ViewModel() {

    val achievements: StateFlow<List<AchievementEntity>> =
        repository.observeAchievements()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
}
