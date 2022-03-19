package dev.szymonchaber.checkstory.domain.model.checklist.fill

data class Checklist(
    val id: ChecklistId,
    val title: String,
    val description: String,
    val items: List<Checkbox>,
    val notes: String
)

val checklist = Checklist(
    ChecklistId("checklistId"),
    "Cleaning living room",
    "I love to have a clean living room, but tend to forget about some hard-to-reach places",
    listOf(
        Checkbox("Table", true),
        Checkbox("Desk", true),
        Checkbox("Floor", false),
        Checkbox("Windows", true),
        Checkbox("Couch", false),
        Checkbox("Chairs", true),
        Checkbox("Shelves", false),
        Checkbox("Shelves", false),
        Checkbox("Shelves", false),
        Checkbox("Shelves", false),
        Checkbox("Shelves", false),
        Checkbox("Shelves", false),
        Checkbox("Shelves", false),
        Checkbox("Shelves", false),
        Checkbox("Shelves", false),
        Checkbox("Shelves", false),
        Checkbox("Shelves", false),
    ),
    "I couldn't clean everything today"
)
