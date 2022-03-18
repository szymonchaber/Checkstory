package dev.szymonchaber.checkstory.domain.model.checklist.fill

data class Checklist(val title: String, val description: String, val items: List<Checkbox>)

val checklist = Checklist(
    "Cleaning living room",
    "I love to have a clean living room, but tend to forget about some hard-to-reach places",
    listOf(
        Checkbox("Table", true),
        Checkbox("Desk", true),
        Checkbox("Floor covers", false)
    )
)
