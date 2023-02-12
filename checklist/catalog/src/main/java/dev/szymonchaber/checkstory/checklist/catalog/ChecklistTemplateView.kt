package dev.szymonchaber.checkstory.checklist.catalog

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Card
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Edit
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
import dev.szymonchaber.checkstory.design.views.DateFormatText
import dev.szymonchaber.checkstory.design.views.SectionLabel
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
        elevation = 2.dp,
        onClick = {
            isCollapsed = !isCollapsed
        }
    ) {
        Column(
            modifier = Modifier
                .animateContentSize()
        ) {
            val interactionSource = remember { MutableInteractionSource() }
            Row(Modifier.fillMaxWidth()) {
                Text(
                    modifier = Modifier
                        .weight(1f)
                        .padding(vertical = 16.dp)
                        .padding(start = 16.dp, end = 8.dp),
                    text = checklistTemplate.title,
                    style = MaterialTheme.typography.subtitle1
                )
                TemplateActions(interactionSource, eventListener, checklistTemplate)
            }
            DateFormatText(
                localDateTime = checklistTemplate.createdAt,
                modifier = Modifier
                    .align(Alignment.End)
                    .padding(horizontal = 16.dp)
                    .padding(top = 16.dp)
            )
            TemplateActionButtons(checklistTemplate, interactionSource, eventListener)
            if (!isCollapsed) {
                SectionLabel(
                    modifier = Modifier.padding(start = 16.dp),
                    text = stringResource(id = R.string.template_checklists)
                )
                ChecklistsCarousel(
                    checklists = checklistTemplate.checklists,
                    paddingValues = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 16.dp),
                    cardElevation = 1.dp,
                    onChecklistClicked = {
                        eventListener(
                            ChecklistCatalogEvent.RecentChecklistClicked(it.id)
                        )
                    }
                ) {
                    Box {
                        Card(
                            modifier = Modifier
                                .size(width = 90.dp, height = 120.dp)
                                .align(Alignment.Center),
                            elevation = 1.dp,
                            onClick = {
                                eventListener(
                                    ChecklistCatalogEvent.NewChecklistFromTemplateClicked(checklistTemplate)
                                )
                            }
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
                }
            }
        }
    }
}

@Composable
private fun RowScope.TemplateActions(
    interactionSource: MutableInteractionSource,
    eventListener: (ChecklistCatalogEvent) -> Unit,
    checklistTemplate: ChecklistTemplate
) {
    var showMenu by remember { mutableStateOf(false) }
    IconButton(
        modifier = Modifier.Companion
            .align(Alignment.Top),
        interactionSource = interactionSource,
        onClick = { showMenu = !showMenu }) {
        Icon(Icons.Default.MoreVert, null, tint = Color.Black)
        DropdownMenu(
            expanded = showMenu,
            onDismissRequest = { showMenu = false }
        ) {
            DropdownMenuItem(onClick = {
                showMenu = false
                eventListener(ChecklistCatalogEvent.EditTemplateClicked(checklistTemplate.id))
            }) {
                Text(text = stringResource(id = R.string.edit))
            }
            DropdownMenuItem(onClick = {
                showMenu = false
                eventListener(ChecklistCatalogEvent.TemplateHistoryClicked(checklistTemplate.id))
            }) {
                Text(text = stringResource(id = R.string.template_checklists))
            }
        }
    }
}

@Composable
private fun TemplateActionButtons(
    checklistTemplate: ChecklistTemplate,
    targetInteractionSource: MutableInteractionSource,
    eventListener: (ChecklistCatalogEvent) -> Unit
) {
    Row(Modifier.fillMaxWidth()) {
        TextButton(
            onClick = {
                eventListener(ChecklistCatalogEvent.NewChecklistFromTemplateClicked(checklistTemplate))
            }
        ) {
            Text(text = stringResource(id = R.string.use).uppercase(), fontWeight = FontWeight.Bold)
        }
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
        ) {
            val editInteractionSource = remember {
                PassThroughInteractionSource(targetInteractionSource)
            }
            val historyInteractionSource = remember {
                PassThroughInteractionSource(targetInteractionSource)
            }
            IconButton(
                interactionSource = editInteractionSource,
                onClick = {}
            ) {
                Icon(imageVector = Icons.Filled.Edit, contentDescription = "Edit")
            }
            IconButton(
                interactionSource = historyInteractionSource,
                onClick = {}
            ) {
                Icon(imageVector = Icons.Filled.DateRange, contentDescription = "History")
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
            "Template",
            "Description",
            listOf(
                TemplateCheckbox(
                    TemplateCheckboxId(0),
                    null,
                    "Checkbox 1",
                    listOf(),
                    0
                )
            ),
            LocalDateTime.now(),
            listOf(),
            listOf()
        ),
        eventListener = {}
    )
}
