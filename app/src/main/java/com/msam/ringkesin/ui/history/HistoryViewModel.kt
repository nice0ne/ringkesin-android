package com.msam.ringkesin.ui.history

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.msam.ringkesin.data.local.AppDatabase
import com.msam.ringkesin.data.local.entity.SessionEntity
import com.msam.ringkesin.data.repository.SessionRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class HistoryUiState(
    val sessions: List<SessionEntity> = emptyList(),
    val searchQuery: String = "",
    val isLoading: Boolean = true,
)

class HistoryViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = SessionRepository(
        AppDatabase.getInstance(application).sessionDao()
    )

    private val _searchQuery = MutableStateFlow("")
    private var searchJob: Job? = null

    private val _uiState = MutableStateFlow(HistoryUiState())
    val uiState: StateFlow<HistoryUiState> = _uiState.asStateFlow()

    init {
        // Observe all sessions with debounced search
        viewModelScope.launch {
            _searchQuery
                .debounce(300)
                .combine(repository.allSessions) { query, sessions ->
                    if (query.isBlank()) sessions
                    else sessions.filter {
                        it.transcript.contains(query, ignoreCase = true) ||
                                it.summary.contains(query, ignoreCase = true)
                    }
                }
                .collect { filtered ->
                    _uiState.value = _uiState.value.copy(
                        sessions = filtered,
                        isLoading = false
                    )
                }
        }
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
        _searchQuery.value = query
    }

    fun deleteSession(session: SessionEntity) {
        viewModelScope.launch {
            repository.delete(session)
        }
    }

    fun restoreSession(session: SessionEntity) {
        val prefs = getApplication<Application>()
            .getSharedPreferences("ringkesin_settings", Application.MODE_PRIVATE)
        prefs.edit()
            .putString("restored_transcript", session.transcript)
            .putString("current_summary", session.summary)
            .apply()
        _uiState.value = _uiState.value.copy()
    }

    fun deleteAll() {
        viewModelScope.launch {
            repository.deleteAll()
        }
    }

    fun getSessionById(id: Long, onResult: (SessionEntity?) -> Unit) {
        viewModelScope.launch {
            onResult(repository.getSessionById(id))
        }
    }
}
