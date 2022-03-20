package dev.szymonchaber.checkstory.checklist.template.model

data class EditTemplateState(
    val templateLoadingState: TemplateLoadingState
) {

    companion object {

        val initial = EditTemplateState(TemplateLoadingState.Loading)
    }
}
