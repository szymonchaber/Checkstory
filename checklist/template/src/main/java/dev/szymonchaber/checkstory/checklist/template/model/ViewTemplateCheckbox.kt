package dev.szymonchaber.checkstory.checklist.template.model

import dev.szymonchaber.checkstory.common.extensions.update
import dev.szymonchaber.checkstory.domain.model.checklist.template.TemplateCheckbox
import dev.szymonchaber.checkstory.domain.model.checklist.template.TemplateCheckboxId

sealed interface ViewTemplateCheckbox {

    val id: TemplateCheckboxId
    val parentId: TemplateCheckboxId?
    val title: String
    val children: List<ViewTemplateCheckbox>

    fun withUpdatedTitle(title: String): ViewTemplateCheckbox

    fun toDomainModel(parentId: TemplateCheckboxId? = null): TemplateCheckbox

    fun plusChildCheckbox(title: String): ViewTemplateCheckbox

    fun minusChildCheckbox(child: ViewTemplateCheckbox): ViewTemplateCheckbox

    fun editChildCheckboxTitle(child: ViewTemplateCheckbox, title: String): ViewTemplateCheckbox

    data class New(
        override val id: TemplateCheckboxId,
        override val parentId: TemplateCheckboxId?,
        override val title: String,
        override val children: List<ViewTemplateCheckbox>
    ) : ViewTemplateCheckbox {

        override fun toDomainModel(parentId: TemplateCheckboxId?): TemplateCheckbox {
            return TemplateCheckbox(TemplateCheckboxId(0), parentId, title, children.map {
                it.toDomainModel(id)
            })
        }

        override fun withUpdatedTitle(title: String): ViewTemplateCheckbox {
            return copy(title = title)
        }

        override fun plusChildCheckbox(title: String): ViewTemplateCheckbox {
            return copy(
                children = children.plus(
                    New(
                        TemplateCheckboxId(children.size.toLong()),
                        null,
                        title,
                        listOf()
                    )
                )
            )
        }

        override fun minusChildCheckbox(child: ViewTemplateCheckbox): ViewTemplateCheckbox {
            return copy(
                children = children.minus(child)
            )
        }

        override fun editChildCheckboxTitle(child: ViewTemplateCheckbox, title: String): ViewTemplateCheckbox {
            return copy(
                children = children.update(child) {
                    it.withUpdatedTitle(title)
                }
            )
        }
    }

    data class Existing(
        override val id: TemplateCheckboxId,
        override val parentId: TemplateCheckboxId?,
        override val title: String,
        override val children: List<ViewTemplateCheckbox>
    ) : ViewTemplateCheckbox {

        override fun toDomainModel(parentId: TemplateCheckboxId?): TemplateCheckbox {
            return TemplateCheckbox(id, parentId, title, children.map {
                it.toDomainModel(id)
            })
        }

        override fun withUpdatedTitle(title: String): ViewTemplateCheckbox {
            return copy(title = title)
        }

        override fun plusChildCheckbox(title: String): ViewTemplateCheckbox {
            return copy(
                children = children.plus(
                    New(
                        TemplateCheckboxId(children.size.toLong()),
                        null,
                        title,
                        listOf()
                    )
                )
            )
        }

        override fun minusChildCheckbox(child: ViewTemplateCheckbox): ViewTemplateCheckbox {
            return copy(
                children = children.minus(child)
            )
        }

        override fun editChildCheckboxTitle(child: ViewTemplateCheckbox, title: String): ViewTemplateCheckbox {
            return copy(
                children = children.update(child) {
                    it.withUpdatedTitle(title)
                }
            )
        }

        companion object {

            fun fromDomainModel(templateCheckbox: TemplateCheckbox): Existing {
                return with(templateCheckbox) {
                    Existing(
                        id,
                        parentId,
                        title,
                        children.map { fromDomainModel(it) }
                    )
                }
            }
        }
    }
}

fun List<ViewTemplateCheckbox>.update(
    viewTemplateCheckbox: ViewTemplateCheckbox,
    updater: (ViewTemplateCheckbox) -> ViewTemplateCheckbox
): List<ViewTemplateCheckbox> {
    return update(viewTemplateCheckbox, { it }, updater)
}
