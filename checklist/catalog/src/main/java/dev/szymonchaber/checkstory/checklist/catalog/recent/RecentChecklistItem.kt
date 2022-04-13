package dev.szymonchaber.checkstory.checklist.catalog.recent

import androidx.compose.foundation.layout.*
import androidx.compose.material.Card
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.szymonchaber.checkstory.checklist.catalog.model.ChecklistCatalogEvent
import dev.szymonchaber.checkstory.design.views.CheckedItemsRatio
import dev.szymonchaber.checkstory.design.views.DateFormatText
import dev.szymonchaber.checkstory.domain.model.checklist.fill.Checkbox
import dev.szymonchaber.checkstory.domain.model.checklist.fill.CheckboxId
import dev.szymonchaber.checkstory.domain.model.checklist.fill.Checklist
import dev.szymonchaber.checkstory.domain.model.checklist.fill.ChecklistId
import dev.szymonchaber.checkstory.domain.model.checklist.template.ChecklistTemplateId
import java.time.LocalDateTime

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun RecentChecklistItem(
    checklist: Checklist,
    eventListener: (ChecklistCatalogEvent) -> Unit
) {
    Card(
        modifier = Modifier
            .widthIn(max = 200.dp),
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
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                CheckedItemsRatio(checklist)
                DateFormatText(checklist.createdAt)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun RecentChecklistItemPreview() {
    val items = listOf(
        Checkbox(CheckboxId(0), "Check this", true),
        Checkbox(CheckboxId(0), "Do not check that", false)
    )
    val checklist = Checklist(
        ChecklistId(0),
        ChecklistTemplateId(0),
        "Recent checklist",
        "Description",
        items,
        "Awesome session!",
        LocalDateTime.now()
    )
    RecentChecklistItem(checklist = checklist, eventListener = {})
}
