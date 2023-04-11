package dev.szymonchaber.checkstory.data.synchronization

import com.google.common.truth.Truth
import org.junit.Test

internal class SynchronizerImplTest {

    @Test
    fun `should create correct state`() {
        val store: SynchronizerImpl = TODO()

        // when
        store.checklistTemplateCreated("id", "initialTitle", "initialDescription", listOf())

        val checklist = store.getState().checklists[0]
        Truth.assertThat(checklist.id).isEqualTo("id")
        Truth.assertThat(checklist.description).isEqualTo("initialDescription")
        Truth.assertThat(checklist.title).isEqualTo("initialTitle")
        Truth.assertThat(checklist.tasks).isEmpty()

        // when
        store.checklistTitleChanged("id", "newTitle")
        val updatedChecklist = store.getState().checklists[0]
        Truth.assertThat(updatedChecklist.title).isEqualTo("newTitle")
    }
}
