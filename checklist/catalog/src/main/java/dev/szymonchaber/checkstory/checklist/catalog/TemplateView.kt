package dev.szymonchaber.checkstory.checklist.catalog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.szymonchaber.checkstory.checklist.catalog.model.ChecklistCatalogEvent
import dev.szymonchaber.checkstory.checklist.catalog.recent.ChecklistsCarousel
import dev.szymonchaber.checkstory.design.R
import dev.szymonchaber.checkstory.design.views.DateFormatText
import dev.szymonchaber.checkstory.design.views.SectionLabel
import dev.szymonchaber.checkstory.domain.model.checklist.template.Template
import dev.szymonchaber.checkstory.domain.model.checklist.template.TemplateId
import dev.szymonchaber.checkstory.domain.model.checklist.template.TemplateTask
import dev.szymonchaber.checkstory.domain.model.checklist.template.TemplateTaskId
import java.time.LocalDateTime
import java.util.*

@Composable
fun TemplateView(
    template: Template,
    eventListener: (ChecklistCatalogEvent) -> Unit
) {
    Column(Modifier.padding(top = 8.dp, bottom = 24.dp)) {
        Row(
            Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    modifier = Modifier
                        .padding(start = 16.dp, end = 8.dp),
                    text = template.title,
                    style = MaterialTheme.typography.subtitle1
                )
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    TextButton(
                        onClick = {
                            eventListener(ChecklistCatalogEvent.UseTemplateClicked(template))
                        }
                    ) {
                        Text(text = stringResource(id = R.string.use).uppercase(), fontWeight = FontWeight.Bold)
                    }
                    DateFormatText(
                        localDateTime = template.createdAt,
                        modifier = Modifier
                            .align(Alignment.CenterVertically)
                    )
                }
            }
            TemplateActions(eventListener, template)
        }
        SectionLabel(
            modifier = Modifier.padding(start = 16.dp),
            text = stringResource(id = R.string.template_checklists)
        )
        ChecklistsCarousel(
            checklists = template.checklists,
            paddingValues = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp),
            onChecklistClicked = {
                eventListener(
                    ChecklistCatalogEvent.RecentChecklistClicked(it.id)
                )
            },
        )
    }
}

@Composable
private fun TemplateActions(
    eventListener: (ChecklistCatalogEvent) -> Unit,
    template: Template
) {
    var showMenu by remember { mutableStateOf(false) }
    IconButton(
        onClick = {
            showMenu = !showMenu
        }
    ) {
        Icon(Icons.Default.MoreVert, null, tint = Color.Black)
        DropdownMenu(
            expanded = showMenu,
            onDismissRequest = {
                showMenu = false
            }
        ) {
            DropdownMenuItem(onClick = {
                showMenu = false
                eventListener(ChecklistCatalogEvent.EditTemplateClicked(template.id))
            }) {
                Text(text = stringResource(id = R.string.edit))
            }
            DropdownMenuItem(onClick = {
                showMenu = false
                eventListener(ChecklistCatalogEvent.TemplateHistoryClicked(template.id))
            }) {
                Text(text = stringResource(id = R.string.template_checklists))
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun TemplateViewPreview() {
    TemplateView(
        template = Template(
            TemplateId.new(),
            "Template",
            "Description",
            listOf(
                TemplateTask(
                    TemplateTaskId(UUID.randomUUID()),
                    null,
                    "Task 1",
                    listOf(),
                    0,
                    TemplateId.new()
                )
            ),
            LocalDateTime.now(),
            listOf(),
            listOf()
        ),
        eventListener = {}
    )
}
