package dev.szymonchaber.checkstory.checklist.catalog.recent

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import dev.szymonchaber.checkstory.domain.model.checklist.fill.Checklist

@Composable
fun ChecklistsCarousel(
    checklists: List<Checklist>,
    paddingValues: PaddingValues,
    onChecklistClicked: (Checklist) -> Unit,
    onDeleteChecklistConfirmed: (Checklist) -> Unit,
    header: @Composable (LazyItemScope.() -> Unit)? = null
) {
    LazyRow(
        contentPadding = paddingValues,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        header?.let {
            item {
                header()
            }
        }
        items(checklists) {
            RecentChecklistItem(
                checklist = it,
                onClick = { onChecklistClicked(it) }
            ) {
                onDeleteChecklistConfirmed(it)
            }
        }
    }
}
