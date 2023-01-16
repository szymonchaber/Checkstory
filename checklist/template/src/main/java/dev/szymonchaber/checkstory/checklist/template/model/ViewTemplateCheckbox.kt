package dev.szymonchaber.checkstory.checklist.template.model

import dev.szymonchaber.checkstory.common.extensions.update
import dev.szymonchaber.checkstory.domain.model.checklist.template.TemplateCheckbox
import dev.szymonchaber.checkstory.domain.model.checklist.template.TemplateCheckboxId

sealed interface ViewTemplateCheckbox : java.io.Serializable {

    val id: TemplateCheckboxId
    val parentId: TemplateCheckboxId?
    val title: String
    val children: List<ViewTemplateCheckbox>
    val isParent: Boolean
        get() = parentId == null

    val isChild: Boolean
        get() = !isParent

    fun withUpdatedTitle(title: String): ViewTemplateCheckbox

    fun toDomainModel(parentId: TemplateCheckboxId? = null): TemplateCheckbox

    fun plusChildCheckbox(title: String): ViewTemplateCheckbox

    fun plusChildCheckbox(viewTemplateCheckbox: ViewTemplateCheckbox, index: Int? = null): ViewTemplateCheckbox

    fun minusChildCheckbox(child: ViewTemplateCheckbox): ViewTemplateCheckbox

    fun editChildCheckboxTitle(child: ViewTemplateCheckbox, title: String): ViewTemplateCheckbox

    fun withUpdatedParentId(parentId: TemplateCheckboxId?): ViewTemplateCheckbox

    data class New(
        override val id: TemplateCheckboxId,
        override val parentId: TemplateCheckboxId?,
        override val title: String,
        override val children: List<ViewTemplateCheckbox>
    ) : ViewTemplateCheckbox {

        override fun toDomainModel(parentId: TemplateCheckboxId?): TemplateCheckbox {
            return TemplateCheckbox(
                id = TemplateCheckboxId(0),
                parentId = parentId,
                title = title,
                children = children.map {
                    it.toDomainModel(id)
                },
                0
            )
        }

        override fun withUpdatedTitle(title: String): ViewTemplateCheckbox {
            return copy(title = title)
        }

        override fun withUpdatedParentId(parentId: TemplateCheckboxId?): ViewTemplateCheckbox {
            return copy(parentId = parentId)
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

        override fun plusChildCheckbox(
            viewTemplateCheckbox: ViewTemplateCheckbox,
            index: Int?
        ): ViewTemplateCheckbox {
            return copy(
                children = children.toMutableList().apply {
                    index?.let {
                        add(it, viewTemplateCheckbox)
                    } ?: kotlin.run {
                        add(
                            viewTemplateCheckbox
                        )
                    }
                }
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
            return TemplateCheckbox(
                id = id,
                parentId = parentId,
                title = title,
                children = children.map {
                    it.toDomainModel(id)
                },
                0
            )
        }

        override fun withUpdatedTitle(title: String): ViewTemplateCheckbox {
            return copy(title = title)
        }

        override fun withUpdatedParentId(parentId: TemplateCheckboxId?): ViewTemplateCheckbox {
            return copy(parentId = parentId)
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

        override fun plusChildCheckbox(
            viewTemplateCheckbox: ViewTemplateCheckbox,
            index: Int?
        ): ViewTemplateCheckbox {
            return copy(
                children = children.toMutableList().apply {
                    index?.let {
                        add(it, viewTemplateCheckbox)
                    } ?: kotlin.run {
                        add(
                            viewTemplateCheckbox
                        )
                    }
                }
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
                        id = id,
                        parentId = parentId,
                        title = title,
                        children = children.map { fromDomainModel(it) }
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
