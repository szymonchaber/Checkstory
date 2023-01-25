package dev.szymonchaber.checkstory.checklist.template

import com.google.common.truth.Truth.assertThat
import dev.szymonchaber.checkstory.checklist.template.model.ViewTemplateCheckbox
import dev.szymonchaber.checkstory.domain.model.checklist.template.TemplateCheckboxId
import org.junit.Test

internal class WithUpdatedPositionKtTest {

    private val initialCheckboxes = checkboxes()
        .flatMap {
            listOf(it) + it.children
        }

    // region original reordering

    @Test(expected = IllegalArgumentException::class)
    fun `when child is dragged to first position, then should throw`() {
        // when
        withUpdatedPosition(initialCheckboxes, initialCheckboxes[1].viewKey, initialCheckboxes[0].viewKey)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `when item is dragged to itself, then should throw`() {
        // when
        withUpdatedPosition(initialCheckboxes, initialCheckboxes[0].viewKey, initialCheckboxes[0].viewKey)
    }

    @Test
    fun `when child is moved within parent, then should reorder children correctly in global list`() {
        // when
        val result = withUpdatedPosition(
            initialCheckboxes,
            initialCheckboxes[1].viewKey,
            initialCheckboxes[2].viewKey
        )

        // then
        assertThat(result[0].title).isEqualTo("Item 1")
        println(result[0].children.joinToString("\n"))
        assertThat(result[1].title).isEqualTo("Child 1-2")
        assertThat(result[2].title).isEqualTo("Child 1-1")
        assertThat(result[3].title).isEqualTo("Child 1-3")
        assertThat(result[4].title).isEqualTo("Item 2")
        assertThat(result[5].title).isEqualTo("Child 2-1")
        assertThat(result[6].title).isEqualTo("Child 2-2")
        assertThat(result[7].title).isEqualTo("Child 2-3")
    }

    @Test
    fun `when parent 1 is moved below parent 2, then should reorder the list keeping children below their parents`() {
        // when
        val result = withUpdatedPosition(
            initialCheckboxes,
            initialCheckboxes[0].viewKey,
            initialCheckboxes[4].viewKey
        )

        // then
        val titles = result.map { it.title }
        assertThat(titles).containsExactly(
            "Item 2",
            "Child 2-1",
            "Child 2-2",
            "Child 2-3",
            "Item 1",
            "Child 1-1",
            "Child 1-2",
            "Child 1-3",
            "Item 3",
            "Child 3-1",
            "Child 3-2",
            "Child 3-3",
            "Item 4",
            "Child 4-1",
            "Child 4-2",
            "Child 4-3"
        ).inOrder()
    }

    @Test
    fun `when parent 2 is moved below parent 3, then should reorder the list keeping children below their parents`() {
        // when
        val result = withUpdatedPosition(
            initialCheckboxes,
            initialCheckboxes[4].viewKey,
            initialCheckboxes[8].viewKey
        )

        // then
        val titles = result.map { it.title }
        assertThat(titles).containsExactly(
            "Item 1",
            "Child 1-1",
            "Child 1-2",
            "Child 1-3",
            "Item 3",
            "Child 3-1",
            "Child 3-2",
            "Child 3-3",
            "Item 2",
            "Child 2-1",
            "Child 2-2",
            "Child 2-3",
            "Item 4",
            "Child 4-1",
            "Child 4-2",
            "Child 4-3"
        ).inOrder()
    }

    @Test
    fun `when parent 2 is moved above parent 1, then should reorder the list keeping children below their parents`() {
        // when
        val result = withUpdatedPosition(
            initialCheckboxes,
            initialCheckboxes[4].viewKey,
            initialCheckboxes[0].viewKey
        )

        // then
        val titles = result.map { it.title }
        assertThat(titles).containsExactly(
            "Item 2",
            "Child 2-1",
            "Child 2-2",
            "Child 2-3",
            "Item 1",
            "Child 1-1",
            "Child 1-2",
            "Child 1-3",
            "Item 3",
            "Child 3-1",
            "Child 3-2",
            "Child 3-3",
            "Item 4",
            "Child 4-1",
            "Child 4-2",
            "Child 4-3"
        ).inOrder()
    }

    @Test
    fun `when parent 3 is moved above parent 2, then should reorder the list keeping children below their parents`() {
        // when
        val result = withUpdatedPosition(
            initialCheckboxes,
            initialCheckboxes[8].viewKey,
            initialCheckboxes[4].viewKey
        )

        // then
        val titles = result.map { it.title }
        assertThat(titles).containsExactly(
            "Item 1",
            "Child 1-1",
            "Child 1-2",
            "Child 1-3",
            "Item 3",
            "Child 3-1",
            "Child 3-2",
            "Child 3-3",
            "Item 2",
            "Child 2-1",
            "Child 2-2",
            "Child 2-3",
            "Item 4",
            "Child 4-1",
            "Child 4-2",
            "Child 4-3"
        ).inOrder()
    }

    @Test
    fun `when child is moved up above its parent, then should move the child in global lists`() {
        // when
        val result = withUpdatedPosition(
            initialCheckboxes,
            initialCheckboxes[5].viewKey,
            initialCheckboxes[4].viewKey
        )

        // then
        assertThat(result[0].title).isEqualTo("Item 1")
        assertThat(result[1].title).isEqualTo("Child 1-1")
        assertThat(result[2].title).isEqualTo("Child 1-2")
        assertThat(result[3].title).isEqualTo("Child 1-3")
        assertThat(result[4].title).isEqualTo("Child 2-1")
        assertThat(result[5].title).isEqualTo("Item 2")
        assertThat(result[6].title).isEqualTo("Child 2-2")
        assertThat(result[7].title).isEqualTo("Child 2-3")
    }

    @Test
    fun `when child is moved below another parent, then should move the child in local and global lists`() {
        // when
        val result = withUpdatedPosition(
            initialCheckboxes,
            initialCheckboxes[3].viewKey,
            initialCheckboxes[4].viewKey
        )

        // then
        assertThat(result[0].title).isEqualTo("Item 1")
        assertThat(result[1].title).isEqualTo("Child 1-1")
        assertThat(result[2].title).isEqualTo("Child 1-2")
        assertThat(result[3].title).isEqualTo("Item 2")
        assertThat(result[4].title).isEqualTo("Child 1-3")
        assertThat(result[5].title).isEqualTo("Child 2-1")
        assertThat(result[6].title).isEqualTo("Child 2-2")
        assertThat(result[7].title).isEqualTo("Child 2-3")
    }

    @Test
    fun `should not crash in this special case`() {
        val from = ViewTemplateCheckbox.New(
            id = TemplateCheckboxId(id = 4),
            parentViewKey = null,
            isParent = true,
            title = "Item 2",
            children = listOf(
                ViewTemplateCheckbox.New(
                    id = TemplateCheckboxId(id = 6),
                    parentViewKey = null,
                    isParent = false,
                    title = "Child 2-2",
                    children = listOf(),
                    isLastChild = false
                ), ViewTemplateCheckbox.New(
                    id = TemplateCheckboxId(id = 7),
                    parentViewKey = null,
                    isParent = false,
                    title = "Child 2-3",
                    children = listOf(),
                    isLastChild = false
                ), ViewTemplateCheckbox.New(
                    id = TemplateCheckboxId(id = 2),
                    parentViewKey = null,
                    isParent = false,
                    title = "Child 1-2",
                    children = listOf(),
                    isLastChild = false
                ), ViewTemplateCheckbox.New(
                    id = TemplateCheckboxId(id = 3),
                    parentViewKey = null,
                    isParent = false,
                    title = "Child 1-3",
                    children = listOf(),
                    isLastChild = false
                )
            ),
            false
        )
        val to = ViewTemplateCheckbox.New(
            id = TemplateCheckboxId(id = 0),
            parentViewKey = null,
            isParent = true,
            title = "Item 1",
            children = listOf(
                ViewTemplateCheckbox.New(
                    id = TemplateCheckboxId(id = 1),
                    parentViewKey = null,
                    isParent = false,
                    title = "Child 1-1",
                    children = listOf(),
                    false
                ),
                ViewTemplateCheckbox.New(
                    id = TemplateCheckboxId(id = 5),
                    parentViewKey = null,
                    isParent = false,
                    title = "Child 2-1",
                    children = listOf(),
                    false
                )
            ),
            false
        )

        val checkboxes = listOf(from, to).flatMap {
            listOf(it) + it.children
        }

        // when
        val result = withUpdatedPosition(
            checkboxes,
            from.viewKey,
            to.viewKey
        )

        // then
        assertThat(result[0].title).isEqualTo("Item 1")
        assertThat(result[1].title).isEqualTo("Child 1-1")
        assertThat(result[2].title).isEqualTo("Child 2-1")
        assertThat(result[3].title).isEqualTo("Item 2")
        assertThat(result[4].title).isEqualTo("Child 2-2")
        assertThat(result[5].title).isEqualTo("Child 2-3")
        assertThat(result[6].title).isEqualTo("Child 1-2")
        assertThat(result[7].title).isEqualTo("Child 1-3")
    }
    // endregion

    // region with wrapping
    @Test
    fun `wrapped when child is moved within parent, then should reorder children correctly in local and global list`() {
        // when
        val result = wrapReorderChanges(
            initialCheckboxes,
            initialCheckboxes[1].viewKey,
            initialCheckboxes[2].viewKey
        )

        // then
        assertThat(result[0].title).isEqualTo("Item 1")
        println(result[0].children.joinToString("\n"))
        assertThat(result[0].children[0].title).isEqualTo("Child 1-2")
        assertThat(result[0].children[1].title).isEqualTo("Child 1-1")
        assertThat(result[0].children[2].title).isEqualTo("Child 1-3")
        assertThat(result[1].title).isEqualTo("Item 2")
    }

    @Test
    fun `wrapped when parent 1 is moved below parent 2, then should reorder the list keeping children below their parents`() {
        // when
        val result = wrapReorderChanges(
            initialCheckboxes,
            initialCheckboxes[0].viewKey,
            initialCheckboxes[4].viewKey
        )

        // then
        assertThat(result[0].title).isEqualTo("Item 2")
        assertThat(result[0].children[0].title).isEqualTo("Child 2-1")
        assertThat(result[0].children[1].title).isEqualTo("Child 2-2")
        assertThat(result[0].children[2].title).isEqualTo("Child 2-3")
        assertThat(result[1].title).isEqualTo("Item 1")
        assertThat(result[1].children[0].title).isEqualTo("Child 1-1")
        assertThat(result[1].children[1].title).isEqualTo("Child 1-2")
        assertThat(result[1].children[2].title).isEqualTo("Child 1-3")
    }

    @Test
    fun `wrapped when parent 2 is moved above parent 1, then should reorder the list keeping children below their parents`() {
        // when
        val result = wrapReorderChanges(
            initialCheckboxes,
            initialCheckboxes[4].viewKey,
            initialCheckboxes[0].viewKey
        )

        // then
        assertThat(result[0].title).isEqualTo("Item 2")
        assertThat(result[1].title).isEqualTo("Item 1")
    }

    @Test
    fun `wrapped when child is moved up above its parent, then should move the child in local and global lists`() {
        // when
        val result = wrapReorderChanges(
            initialCheckboxes,
            initialCheckboxes[5].viewKey,
            initialCheckboxes[4].viewKey
        )

        // then
        assertThat(result[0].title).isEqualTo("Item 1")
        assertThat(result[0].children[0].title).isEqualTo("Child 1-1")
        assertThat(result[0].children[1].title).isEqualTo("Child 1-2")
        assertThat(result[0].children[2].title).isEqualTo("Child 1-3")
        assertThat(result[0].children[3].title).isEqualTo("Child 2-1")
        assertThat(result[1].title).isEqualTo("Item 2")
        assertThat(result[1].children[0].title).isEqualTo("Child 2-2")
        assertThat(result[1].children[1].title).isEqualTo("Child 2-3")
    }

    @Test
    fun `wrapped when child is moved below another parent, then should move the child in local and global lists`() {
        // when
        val result = wrapReorderChanges(
            initialCheckboxes,
            initialCheckboxes[3].viewKey,
            initialCheckboxes[4].viewKey
        )

        // then
        assertThat(result[0].title).isEqualTo("Item 1")
        assertThat(result[0].children[0].title).isEqualTo("Child 1-1")
        assertThat(result[0].children[1].title).isEqualTo("Child 1-2")
        assertThat(result[1].title).isEqualTo("Item 2")
        assertThat(result[1].children[0].title).isEqualTo("Child 1-3")
        assertThat(result[1].children[1].title).isEqualTo("Child 2-1")
        assertThat(result[1].children[2].title).isEqualTo("Child 2-2")
        assertThat(result[1].children[3].title).isEqualTo("Child 2-3")
    }

    @Test
    fun `wrapped should not crash in this special case`() {
        val from = ViewTemplateCheckbox.New(
            id = TemplateCheckboxId(id = 4),
            parentViewKey = null,
            isParent = true,
            title = "Item 2",
            children = listOf(
                ViewTemplateCheckbox.New(
                    id = TemplateCheckboxId(id = 6),
                    parentViewKey = null,
                    isParent = false,
                    title = "Child 2-2",
                    children = listOf(),
                    isLastChild = false
                ), ViewTemplateCheckbox.New(
                    id = TemplateCheckboxId(id = 7),
                    parentViewKey = null,
                    isParent = false,
                    title = "Child 2-3",
                    children = listOf(),
                    isLastChild = false
                ), ViewTemplateCheckbox.New(
                    id = TemplateCheckboxId(id = 2),
                    parentViewKey = null,
                    isParent = false,
                    title = "Child 1-2",
                    children = listOf(),
                    isLastChild = false
                ), ViewTemplateCheckbox.New(
                    id = TemplateCheckboxId(id = 3),
                    parentViewKey = null,
                    isParent = false,
                    title = "Child 1-3",
                    children = listOf(),
                    isLastChild = false
                )
            ),
            isLastChild = false
        )
        val to = ViewTemplateCheckbox.New(
            id = TemplateCheckboxId(id = 0),
            parentViewKey = null,
            isParent = true,
            title = "Item 1",
            children = listOf(
                ViewTemplateCheckbox.New(
                    id = TemplateCheckboxId(id = 1),
                    parentViewKey = null,
                    isParent = false,
                    title = "Child 1-1",
                    children = listOf(),
                    isLastChild = false
                ),
                ViewTemplateCheckbox.New(
                    id = TemplateCheckboxId(id = 5),
                    parentViewKey = null,
                    isParent = false,
                    title = "Child 2-1",
                    children = listOf(),
                    isLastChild = false
                )
            ),
            isLastChild = false
        )

        val checkboxes = listOf(from, to).flatMap {
            listOf(it.replaceChildren(listOf())) + it.children
        }

        // when
        val actualFrom = from.replaceChildren(listOf())
        val actualTo = to.replaceChildren(listOf())
        val result = wrapReorderChanges(
            checkboxes,
            actualFrom.viewKey,
            actualTo.viewKey
        )

        // then
        assertThat(result[0].title).isEqualTo("Item 1")
        assertThat(result[0].children[0].title).isEqualTo("Child 1-1")
        assertThat(result[0].children[1].title).isEqualTo("Child 2-1")
        assertThat(result[1].title).isEqualTo("Item 2")
        assertThat(result[1].children[0].title).isEqualTo("Child 2-2")
        assertThat(result[1].children[1].title).isEqualTo("Child 2-3")
        assertThat(result[1].children[2].title).isEqualTo("Child 1-2")
        assertThat(result[1].children[3].title).isEqualTo("Child 1-3")
    }
    // endregion

    private fun checkboxes(): List<ViewTemplateCheckbox.New> {
        var globalIndex = 0L
        return List(4) { parentIndex ->
            ViewTemplateCheckbox.New(
                TemplateCheckboxId(globalIndex++),
                null,
                true,
                "Item ${parentIndex + 1}",
                List(3) {
                    ViewTemplateCheckbox.New(
                        TemplateCheckboxId(globalIndex++),
                        null,
                        false,
                        "Child ${parentIndex + 1}-${it + 1}",
                        listOf(),
                        false
                    )
                },
                false
            )
        }
    }
}
