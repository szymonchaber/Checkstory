package dev.szymonchaber.checkstory.checklist.catalog

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Card
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.szymonchaber.checkstory.checklist.catalog.model.ChecklistCatalogEvent
import dev.szymonchaber.checkstory.domain.model.checklist.template.ChecklistTemplate

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
            eventListener(ChecklistCatalogEvent.ChecklistTemplateClicked(checklistTemplate.title)) // TODO change to id
        }
    ) {
        Text(
            modifier = Modifier.padding(16.dp),
            text = checklistTemplate.title,
            style = MaterialTheme.typography.subtitle1
        )
    }
}
