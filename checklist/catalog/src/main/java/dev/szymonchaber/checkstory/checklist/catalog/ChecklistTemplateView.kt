package dev.szymonchaber.checkstory.checklist.catalog

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Edit
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.szymonchaber.checkstory.checklist.catalog.model.ChecklistCatalogEvent
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
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp),
        elevation = 4.dp,
        onClick = {
            eventListener(ChecklistCatalogEvent.TemplateClicked(checklistTemplate.id))
        }
    ) {
        Column {
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
                    .padding(top = 16.dp, bottom = 8.dp)
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
                    "Checkbox 1"
                )
            ),
            LocalDateTime.now()
        ),
        eventListener = {}
    )
}
