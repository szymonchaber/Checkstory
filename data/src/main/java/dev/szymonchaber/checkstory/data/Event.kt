package dev.szymonchaber.checkstory.data

sealed interface Event {

    fun State.apply(): State

    data class TemplateCreated(val id: String, val title: String, val description: String, val tasks: List<String>) :
        Event {

        override fun State.apply(): State {
            return copy(
                checklists = checklists.plus(
                    Checklist(
                        id,
                        title,
                        description,
                        tasks
                    )
                )
            )
        }
    }

    data class TemplateTitleChanged(val id: String, val newTitle: String) : Event {
        override fun State.apply(): State {
            val checklist = checklists.find { it.id == id } ?: error("Could not find the checklist with id $id")
            val filteredChecklists = checklists.minus(checklist)
            return copy(
                checklists = filteredChecklists.plus(
                    checklist.copy(title = newTitle)
                )
            )
        }
    }
}

data class State(val checklists: List<Checklist> = listOf())

data class Checklist(val id: String, val title: String, val description: String, val tasks: List<String>)
