package dev.szymonchaber.checkstory.checklist.catalog.recent

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.Card
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.szymonchaber.checkstory.checklist.catalog.model.ChecklistCatalogEvent
import dev.szymonchaber.checkstory.design.views.DateFormatText
import dev.szymonchaber.checkstory.domain.model.checklist.fill.Checklist

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun RecentChecklistItem(
    checklist: Checklist,
    eventListener: (ChecklistCatalogEvent) -> Unit
) {
    Card(
        modifier = Modifier
            .widthIn(max = 160.dp),
        elevation = 4.dp,
        onClick = {
            eventListener(ChecklistCatalogEvent.RecentChecklistClicked(checklist.id))
        }
    ) {
        val notes by remember {
            mutableStateOf(checklist.notes.takeUnless(String::isBlank)?.let { "\"$it\"" } ?: "🙊")
        }
        Column(
            modifier = Modifier.padding(all = 16.dp)
        ) {
            Text(
                text = checklist.title,
                style = MaterialTheme.typography.subtitle1
            )
            Text(
                modifier = Modifier.padding(top = 16.dp),
                text = notes,
                style = MaterialTheme.typography.subtitle1,
                maxLines = 1
            )
            DateFormatText(
                modifier = Modifier
                    .padding(top = 16.dp)
                    .align(Alignment.End),
                localDateTime = checklist.createdAt
            )
        }

    }
}
