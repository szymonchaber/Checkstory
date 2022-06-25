package dev.szymonchaber.checkstory.checklist.catalog

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Edit
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.szymonchaber.checkstory.checklist.catalog.model.ChecklistCatalogEvent
import dev.szymonchaber.checkstory.checklist.catalog.recent.ChecklistsCarousel
import dev.szymonchaber.checkstory.design.views.DateFormatText
import dev.szymonchaber.checkstory.domain.model.checklist.template.ChecklistTemplate
import dev.szymonchaber.checkstory.domain.model.checklist.template.ChecklistTemplateId
import dev.szymonchaber.checkstory.domain.model.checklist.template.TemplateCheckbox
import dev.szymonchaber.checkstory.domain.model.checklist.template.TemplateCheckboxId
import java.time.LocalDateTime

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun ChecklistTemplateView(
    checklistTemplate: ChecklistTemplate,
    eventListener: (ChecklistCatalogEvent) -> Unit
) {
    var isCollapsed by remember { mutableStateOf(true) }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(top = 8.dp),
        elevation = 4.dp,
        onClick = {
            isCollapsed = !isCollapsed
        }
    ) {
        Column(
            modifier = Modifier
                .animateContentSize()
        ) {
            Text(
                modifier = Modifier.padding(16.dp),
                text = checklistTemplate.title,
                style = MaterialTheme.typography.subtitle1
            )
            DateFormatText(
                localDateTime = checklistTemplate.createdAt,
                modifier = Modifier
                    .align(Alignment.End)
                    .padding(horizontal = 16.dp)
                    .padding(top = 16.dp)
            )
            Row(
                modifier = Modifier.align(Alignment.End),
            ) {
                IconButton(
                    onClick = {
                        eventListener(
                            ChecklistCatalogEvent.EditTemplateClicked(checklistTemplate.id)
                        )
                    }
                ) {
                    Icon(imageVector = Icons.Filled.Edit, contentDescription = "Edit")
                }
                IconButton(
                    onClick = {
                        eventListener(
                            ChecklistCatalogEvent.TemplateHistoryClicked(checklistTemplate.id)
                        )
                    }
                ) {
                    Icon(imageVector = Icons.Filled.DateRange, contentDescription = "History")
                }
            }
            if (!isCollapsed) {
                ChecklistsCarousel(checklistTemplate.checklists, {
                    Box {
                        Card(
                            modifier = Modifier
                                .size(width = 90.dp, height = 115.dp)
                                .align(Alignment.Center),
                            elevation = 4.dp,
                            onClick = { eventListener(ChecklistCatalogEvent.TemplateClicked(checklistTemplate.id)) }
                        ) {
                            Box {
                                Icon(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .align(Alignment.Center),
                                    imageVector = Icons.Filled.Add,
                                    contentDescription = "Add"
                                )
                            }
                        }
                    }
                }) {
                    eventListener(
                        ChecklistCatalogEvent.RecentChecklistClicked(it.id)
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ChecklistTemplateViewPreview() {
    ChecklistTemplateView(
        checklistTemplate = ChecklistTemplate(
            ChecklistTemplateId(0),
            "Checklist template",
            "Checklist description",
            listOf(
                TemplateCheckbox(
                    TemplateCheckboxId(0),
                    null,
                    "Checkbox 1",
                    listOf()
                )
            ),
            LocalDateTime.now(),
            listOf(),
            listOf()
        ),
        eventListener = {}
    )
}
