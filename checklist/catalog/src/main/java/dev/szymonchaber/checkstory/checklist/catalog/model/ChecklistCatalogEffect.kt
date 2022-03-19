package dev.szymonchaber.checkstory.checklist.catalog.model

import dev.szymonchaber.checkstory.domain.model.checklist.template.ChecklistTemplateId

sealed interface ChecklistCatalogEffect {

    data class CreateAndNavigateToChecklist(val basedOn: ChecklistTemplateId) : ChecklistCatalogEffect
}
