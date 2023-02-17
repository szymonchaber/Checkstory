package dev.szymonchaber.checkstory.checklist.template.model

import dev.szymonchaber.checkstory.domain.model.checklist.template.TemplateCheckboxId

class CheckboxesScope {

    val checkboxes = mutableMapOf<String, MutableList<String>>()

    inner class ChildScope(private val parent: String) {

        fun childCheckbox(title: String) {
            checkboxes[parent] = checkboxes[parent]?.plus(title)?.toMutableList() ?: mutableListOf(title)
        }
    }

    fun checkbox(title: String, children: ChildScope.() -> Unit) {
        checkboxes[title] = mutableListOf()
        ChildScope(title).children()
    }

    companion object {

        fun checkboxes(block: CheckboxesScope.() -> Unit): List<ViewTemplateCheckbox> {
            val checkboxesScope = CheckboxesScope()
            checkboxesScope.block()
            return checkboxesScope.checkboxes.toList().mapIndexed { index, (title, children) ->
                ViewTemplateCheckbox.New(
                    TemplateCheckboxId(index.toLong()), null, true, title, children.mapIndexed { index, title ->
                        ViewTemplateCheckbox.New(
                            TemplateCheckboxId(index.toLong()),
                            null,
                            false,
                            title,
                            listOf(),
                            false
                        )
                    }, false
                )
            }
        }
    }
}
