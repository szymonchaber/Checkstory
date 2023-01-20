package dev.szymonchaber.checkstory.checklist.template.model

import dev.szymonchaber.checkstory.checklist.template.ViewTemplateCheckboxKey
import dev.szymonchaber.checkstory.checklist.template.viewKey
import dev.szymonchaber.checkstory.common.extensions.update
import dev.szymonchaber.checkstory.domain.model.checklist.template.TemplateCheckbox
import dev.szymonchaber.checkstory.domain.model.checklist.template.TemplateCheckboxId

sealed interface ViewTemplateCheckbox : java.io.Serializable {

    val id: TemplateCheckboxId
    val parentViewKey: ViewTemplateCheckboxKey?
    val parentId: TemplateCheckboxId?
        get() = parentViewKey?.viewId?.let(::TemplateCheckboxId)
    val title: String
    val children: List<ViewTemplateCheckbox>
    val isParent: Boolean

    val isChild: Boolean
        get() = !isParent

    fun withUpdatedTitle(title: String): ViewTemplateCheckbox

    fun toDomainModel(parentId: TemplateCheckboxId? = null): TemplateCheckbox

    fun plusChildCheckbox(title: String): ViewTemplateCheckbox

    fun plusChildCheckbox(viewTemplateCheckbox: ViewTemplateCheckbox, index: Int? = null): ViewTemplateCheckbox

    fun minusChildCheckbox(child: ViewTemplateCheckbox): ViewTemplateCheckbox

    fun editChildCheckboxTitle(child: ViewTemplateCheckbox, title: String): ViewTemplateCheckbox

    fun replaceChildren(children: List<ViewTemplateCheckbox>): ViewTemplateCheckbox

    data class New(
        override val id: TemplateCheckboxId,
        override val parentViewKey: ViewTemplateCheckboxKey?,
        override val isParent: Boolean,
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

        override fun plusChildCheckbox(title: String): ViewTemplateCheckbox {
            return copy(
                children = children.plus(
                    New(
                        TemplateCheckboxId(children.size.toLong()),
                        viewKey,
                        false,
                        "",
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

        override fun replaceChildren(children: List<ViewTemplateCheckbox>): ViewTemplateCheckbox {
            return copy(children = children)
        }
    }

    data class Existing(
        override val id: TemplateCheckboxId,
        override val parentViewKey: ViewTemplateCheckboxKey?,
        override val isParent: Boolean,
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

        override fun plusChildCheckbox(title: String): ViewTemplateCheckbox {
            return copy(
                children = children.plus(
                    New(
                        TemplateCheckboxId(children.size.toLong()),
                        viewKey,
                        false,
                        "",
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

        override fun replaceChildren(children: List<ViewTemplateCheckbox>): ViewTemplateCheckbox {
            return copy(children = children)
        }

        companion object {

            fun fromDomainModel(templateCheckbox: TemplateCheckbox): Existing {
                return with(templateCheckbox) {
                    Existing(
                        id = id,
                        parentViewKey = parentId?.let {
                            ViewTemplateCheckboxKey(viewId = it.id, isNew = false, isParent = true, parentKey = null)
                        },
                        parentId == null,
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
    return update(
        viewTemplateCheckbox.id,
        {
            it.id // TODO it was "it" before - will it work still?
        },
        updater
    )
}

fun List<ViewTemplateCheckbox>.update(
    templateCheckboxId: TemplateCheckboxId,
    updater: (ViewTemplateCheckbox) -> ViewTemplateCheckbox
): List<ViewTemplateCheckbox> {
    return update(
        templateCheckboxId,
        {
            it.id // TODO it was "it" before - will it work still?
        },
        updater
    )
}

fun List<ViewTemplateCheckbox>.update(
    viewTemplateCheckboxKey: ViewTemplateCheckboxKey,
    updater: (ViewTemplateCheckbox) -> ViewTemplateCheckbox
): List<ViewTemplateCheckbox> {
    return update(
        viewTemplateCheckboxKey,
        {
            it.viewKey
        },
        updater
    )
}

fun renderCheckboxes(templateCheckboxes: List<ViewTemplateCheckbox>): String {
    return templateCheckboxes.joinToString("\n") {
        val parentIndicator = if (it.isParent) "Parent " else "Child "
        val children = it.children.takeUnless { it.isEmpty() }?.joinToString("\n") { child ->
            val childIndicator = if (child.isParent) "Parent " else "Child "
            "     $childIndicator ${child.title}"
        }
        parentIndicator + " " + it.title + children?.let { "\n" + it }.orEmpty()
    }
}
