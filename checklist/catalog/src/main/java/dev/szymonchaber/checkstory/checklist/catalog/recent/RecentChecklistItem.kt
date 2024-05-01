package dev.szymonchaber.checkstory.checklist.catalog.recent

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.Card
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.szymonchaber.checkstory.design.R
import dev.szymonchaber.checkstory.design.views.CheckedItemsRatio
import dev.szymonchaber.checkstory.design.views.DateFormatText
import dev.szymonchaber.checkstory.domain.model.checklist.fill.Checklist
import dev.szymonchaber.checkstory.domain.model.checklist.fill.ChecklistId
import dev.szymonchaber.checkstory.domain.model.checklist.fill.Task
import dev.szymonchaber.checkstory.domain.model.checklist.fill.TaskId
import dev.szymonchaber.checkstory.domain.model.checklist.template.TemplateId
import java.time.LocalDateTime
import java.util.*

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun RecentChecklistItem(
    modifier: Modifier = Modifier,
    checklist: Checklist,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .width(110.dp),
        elevation = 2.dp,
        onClick = onClick
    ) {
        Column(
            modifier = Modifier.padding(all = 8.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                CheckedItemsRatio(
                    modifier = Modifier.align(Alignment.CenterVertically),
                    checklist = checklist,
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            NotesTextView(checklist.notes)
            Spacer(modifier = Modifier.height(8.dp))
            DateFormatText(checklist.createdAt)
        }
    }
}

@Composable
private fun NotesTextView(notes: String) {
    val notesFontStyle = remember(notes) {
        if (notes.isBlank()) {
            FontStyle.Italic
        } else {
            FontStyle.Normal
        }
    }
    val blankText = stringResource(id = R.string.no_notes)
    val notesOrEmptyNotesText = remember(notes, blankText) {
        notes.ifBlank {
            blankText
        }
    }
    Text(
        text = notesOrEmptyNotesText,
        style = MaterialTheme.typography.subtitle1.copy(fontStyle = notesFontStyle),
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
    )
}

@Preview(showBackground = true)
@Composable
fun RecentChecklistItemPreview() {
    val items = listOf(
        Task(TaskId(UUID.randomUUID()), null, ChecklistId.new(), "Check this", true, listOf(), 0),
        Task(
            TaskId(UUID.randomUUID()),
            null,
            ChecklistId.new(),
            "Do not check that",
            false,
            listOf(),
            0
        )
    )
    val checklist = Checklist(
        ChecklistId.new(),
        TemplateId.new(),
        "Recent checklist",
        "Description",
        items,
        "Awesome session!",
        LocalDateTime.now()
    )
    RecentChecklistItem(
        checklist = checklist
    ) { }
}
