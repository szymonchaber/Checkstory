package dev.szymonchaber.checkstory.checklist.catalog.recent

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.szymonchaber.checkstory.checklist.catalog.R
import dev.szymonchaber.checkstory.checklist.catalog.model.ChecklistCatalogEvent
import dev.szymonchaber.checkstory.checklist.catalog.model.RecentChecklistsLoadingState
import dev.szymonchaber.checkstory.domain.model.checklist.fill.Checkbox
import dev.szymonchaber.checkstory.domain.model.checklist.fill.CheckboxId
import dev.szymonchaber.checkstory.domain.model.checklist.fill.Checklist
import dev.szymonchaber.checkstory.domain.model.checklist.fill.ChecklistId
import dev.szymonchaber.checkstory.domain.model.checklist.template.ChecklistTemplateId
import java.time.LocalDateTime

@Composable
fun ColumnScope.RecentChecklistsView(
    loadingState: RecentChecklistsLoadingState,
    eventListener: (ChecklistCatalogEvent) -> Unit = {}
) {
    Text(
        modifier = Modifier.padding(start = 16.dp, end = 16.dp),
        text = stringResource(R.string.recent_checklists),
        style = MaterialTheme.typography.h5
    )
    when (val state = loadingState) {
        RecentChecklistsLoadingState.Loading -> {
            CircularProgressIndicator(
                modifier = Modifier
                    .align(alignment = Alignment.CenterHorizontally)
                    .padding(top = 24.dp)
            )
        }
        is RecentChecklistsLoadingState.Success -> {
            LazyRow(
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(state.checklists) {
                    RecentChecklistItem(it, eventListener)
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun RecentChecklistsLoadingPreview() {
    Column {
        RecentChecklistsView(loadingState = RecentChecklistsLoadingState.Loading)
    }
}

@Preview(showBackground = true)
@Composable
fun RecentChecklistsSuccessPreview() {
    Column {
        val items = listOf(
            Checkbox(CheckboxId(0), "Check this", true),
            Checkbox(CheckboxId(0), "Do not check that", false)
        )
        val checklists = listOf(
            Checklist(
                ChecklistId(0),
                ChecklistTemplateId(0),
                "Recent checklist",
                "Description",
                items,
                "Awesome session!",
                LocalDateTime.now()
            )
        )
        RecentChecklistsView(loadingState = RecentChecklistsLoadingState.Success(checklists))
    }
}
