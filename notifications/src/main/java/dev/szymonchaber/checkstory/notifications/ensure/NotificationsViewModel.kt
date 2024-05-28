package dev.szymonchaber.checkstory.notifications.ensure

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.szymonchaber.checkstory.domain.model.checklist.template.reminder.Reminder
import dev.szymonchaber.checkstory.domain.repository.TemplateReminderRepository
import dev.szymonchaber.checkstory.domain.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class NotificationsViewModel @Inject constructor(
    private val reminderRepository: TemplateReminderRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _state = MutableStateFlow(NotificationsState())
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            reminderRepository.getAllReminders()
                .map(List<Reminder>::isNotEmpty)
                .map { isPermissionRequired ->
                    !state.value.didUserDismissWarning && isPermissionRequired
                }
                .collect { showDialog ->
                    _state.update {
                        it.copy(checkNotificationPermissionMissing = showDialog)
                    }
                }
        }
        viewModelScope.launch {
            userRepository.getCurrentUserFlow().collect {
                _state.update {
                    it.copy(didUserDismissWarning = false)
                }
            }
        }
    }

    fun onUserDismissed() {
        _state.update {
            it.copy(checkNotificationPermissionMissing = false, didUserDismissWarning = true)
        }
    }
}
