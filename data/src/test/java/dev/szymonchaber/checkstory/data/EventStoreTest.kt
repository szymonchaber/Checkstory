package dev.szymonchaber.checkstory.data

import com.google.common.truth.Truth.assertThat
import org.junit.Test

internal class EventStoreTest {

    @Test
    fun `should create correct state`() {
        val store = EventStore()

        // when
        store.checklistTemplateCreated("id", "initialTitle", "initialDescription", listOf())

        val checklist = store.getState().checklists[0]
        assertThat(checklist.id).isEqualTo("id")
        assertThat(checklist.description).isEqualTo("initialDescription")
        assertThat(checklist.title).isEqualTo("initialTitle")
        assertThat(checklist.tasks).isEmpty()

        // when
        store.checklistTitleChanged("id", "newTitle")
        val updatedChecklist = store.getState().checklists[0]
        assertThat(updatedChecklist.title).isEqualTo("newTitle")
    }
}
