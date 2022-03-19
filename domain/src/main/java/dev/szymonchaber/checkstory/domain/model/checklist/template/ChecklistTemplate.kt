package dev.szymonchaber.checkstory.domain.model.checklist.template

data class ChecklistTemplate(
    val title: String,
    val description: String,
    val items: List<TemplateCheckbox>
)

data class TemplateCheckbox(val title: String)

val checklistTemplate = ChecklistTemplate(
    "Cleaning living room",
    "I love to have a clean living room, but tend to forget about some hard-to-reach places",
    listOf(
        TemplateCheckbox("Table"),
        TemplateCheckbox("Desk"),
        TemplateCheckbox("Floor"),
        TemplateCheckbox("Windows"),
        TemplateCheckbox("Couch"),
        TemplateCheckbox("Chairs"),
        TemplateCheckbox("Shelves"),
        TemplateCheckbox("Shelves"),
    ),
)
