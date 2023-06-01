package dev.szymonchaber.checkstory.checklist.catalog.recent

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.Card
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.szymonchaber.checkstory.checklist.catalog.R
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
    cardElevation: Dp,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .widthIn(max = 200.dp),
        elevation = cardElevation,
        onClick = onClick
    ) {
        Column(
            modifier = Modifier.padding(all = 16.dp)
        ) {
            Text(
                text = checklist.title,
                style = MaterialTheme.typography.subtitle1,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            NotesTextView(checklist.notes)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                CheckedItemsRatio(checklist)
                DateFormatText(checklist.createdAt)
            }
        }
    }
}

@Composable
private fun NotesTextView(notes: String) {
    val notesFontStyle = if (notes.isBlank()) {
        FontStyle.Italic
    } else {
        FontStyle.Normal
    }
    val notesOrEmptyNotesText = notes.ifBlank {
        stringResource(id = R.string.no_notes)
    }
    Text(
        modifier = Modifier.padding(top = 8.dp),
        text = notesOrEmptyNotesText,
        style = MaterialTheme.typography.subtitle1.copy(fontStyle = notesFontStyle, fontSize = 14.sp),
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
    )
}

@Preview(showBackground = true)
@Composable
fun RecentChecklistItemPreview() {
    val items = listOf(
        Task(TaskId(UUID.randomUUID()), null, ChecklistId.new(), "Check this", true, listOf()),
        Task(
            TaskId(UUID.randomUUID()),
            null,
            ChecklistId.new(),
            "Do not check that",
            false,
            listOf()
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
        checklist = checklist,
        cardElevation = 1.dp
    ) { }
}
