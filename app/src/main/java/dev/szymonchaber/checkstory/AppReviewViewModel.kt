package dev.szymonchaber.checkstory

import androidx.core.os.bundleOf
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.szymonchaber.checkstory.common.Tracker
import dev.szymonchaber.checkstory.domain.usecase.GetChecklistSavedEventsUseCase
import dev.szymonchaber.checkstory.domain.usecase.GetRecentChecklistsUseCase
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.take
import javax.inject.Inject

@HiltViewModel
class AppReviewViewModel @Inject constructor(
    getChecklistSavedEventsUseCase: GetChecklistSavedEventsUseCase,
    private val getRecentChecklistsUseCase: GetRecentChecklistsUseCase,
    private val tracker: Tracker
) : ViewModel() {

    val displayReviewEventFlow = getChecklistSavedEventsUseCase.getChecklistSavedEvents()
        .flatMapLatest {
            getRecentChecklistsUseCase.getRecentChecklists()
        }
        .map {
            it.count()
        }
        .filter { savedChecklistsCount ->
            savedChecklistsCount >= CHECKLISTS_REQUIRED_TO_DISPLAY_REVIEW
        }
        .onEach { savedChecklistsCount ->
            tracker.logEvent(
                "in_app_review_dialog_requested",
                bundleOf("total_saved_checklist_count" to savedChecklistsCount)
            )
        }
        .map {
            RequestInAppReviewDialog
        }
        .take(1)

    companion object {

        private const val CHECKLISTS_REQUIRED_TO_DISPLAY_REVIEW = 3
    }
}
