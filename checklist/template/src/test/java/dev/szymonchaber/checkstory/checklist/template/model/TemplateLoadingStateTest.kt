package dev.szymonchaber.checkstory.checklist.template.model

import com.google.common.truth.Truth.assertThat
import dev.szymonchaber.checkstory.domain.model.checklist.template.ChecklistTemplate
import dev.szymonchaber.checkstory.domain.model.checklist.template.ChecklistTemplateId
import dev.szymonchaber.checkstory.domain.model.checklist.template.TemplateCheckbox
import dev.szymonchaber.checkstory.domain.model.checklist.template.TemplateCheckboxId
import org.junit.Test
import java.time.LocalDateTime

class TemplateLoadingStateTest {

    @Test
    fun `given existing and new item with the same id, should update item of the same type only`() {
        // given
        val commonCheckboxId = TemplateCheckboxId(1)
        val successState = success(listOf(TemplateCheckbox(commonCheckboxId, null, "title", listOf(), 0)))

        // when
        val updatedState = successState.plusNewCheckbox("New checkbox").let {
            val newCheckbox = it.checkboxes.filterIsInstance<ViewTemplateCheckbox.New>().first()
            it
        }

        // then
        assertThat(updatedState.checkboxes).containsExactly(
            ViewTemplateCheckbox.Existing(commonCheckboxId, null, true, "title", listOf()),
            ViewTemplateCheckbox.New(
                commonCheckboxId, null, true, "New checkbox", listOf(
                    ViewTemplateCheckbox.New(TemplateCheckboxId(0), null, true, "New child checkbox", listOf())
                )
            )
        )
    }

    private fun success(list: List<TemplateCheckbox>): TemplateLoadingState.Success {
        return TemplateLoadingState.Success.fromTemplate(
            ChecklistTemplate(
                ChecklistTemplateId(0),
                "Template",
                "Description",
                list,
                LocalDateTime.now(),
                listOf(),
                listOf()
            )
        )
    }
}
