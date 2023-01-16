package dev.szymonchaber.checkstory.checklist.template

import com.google.common.truth.Truth.assertThat
import dev.szymonchaber.checkstory.checklist.template.model.ViewTemplateCheckbox
import dev.szymonchaber.checkstory.domain.model.checklist.template.TemplateCheckboxId
import org.burnoutcrew.reorderable.ItemPosition
import org.junit.Test

internal class WithUpdatedPositionKtTest {

    private val initialCheckboxes = checkboxes()
        .flatMap {
            listOf(it) + it.children
        }

    @Test
    fun `when child is moved within parent, then should reorder children correctly in local and global list`() {
        // when
        val result = withUpdatedPosition(
            initialCheckboxes,
            ItemPosition(0, initialCheckboxes[1]),
            ItemPosition(0, initialCheckboxes[2])
        )

        // then
        assertThat(result[0].title).isEqualTo("Item 1")
        println(result[0].children.joinToString("\n"))
        assertThat(result[0].children[0].title).isEqualTo("Child 1-2")
        assertThat(result[0].children[1].title).isEqualTo("Child 1-1")
        assertThat(result[0].children[2].title).isEqualTo("Child 1-3")
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
            ItemPosition(0, initialCheckboxes[0]),
            ItemPosition(0, initialCheckboxes[4])
        )

        // then
        assertThat(result[0].title).isEqualTo("Item 2")
        assertThat(result[1].title).isEqualTo("Child 2-1")
        assertThat(result[2].title).isEqualTo("Child 2-2")
        assertThat(result[3].title).isEqualTo("Child 2-3")
        assertThat(result[4].title).isEqualTo("Item 1")
        assertThat(result[5].title).isEqualTo("Child 1-1")
        assertThat(result[6].title).isEqualTo("Child 1-2")
        assertThat(result[7].title).isEqualTo("Child 1-3")
    }

    @Test
    fun `when parent 2 is moved above parent 1, then should reorder the list keeping children below their parents`() {
        // when
        val result = withUpdatedPosition(
            initialCheckboxes,
            ItemPosition(0, initialCheckboxes[4]),
            ItemPosition(0, initialCheckboxes[0])
        )

        // then
        assertThat(result[0].title).isEqualTo("Item 2")
        assertThat(result[1].title).isEqualTo("Child 2-1")
        assertThat(result[2].title).isEqualTo("Child 2-2")
        assertThat(result[3].title).isEqualTo("Child 2-3")
        assertThat(result[4].title).isEqualTo("Item 1")
        assertThat(result[5].title).isEqualTo("Child 1-1")
        assertThat(result[6].title).isEqualTo("Child 1-2")
        assertThat(result[7].title).isEqualTo("Child 1-3")
    }

    @Test
    fun `when child is moved up above its parent, then should move the child in local and global lists`() {
        // when
        val result = withUpdatedPosition(
            initialCheckboxes,
            ItemPosition(0, initialCheckboxes[5]),
            ItemPosition(0, initialCheckboxes[4])
        )

        // then
        assertThat(result[0].title).isEqualTo("Item 1")
        assertThat(result[0].children[0].title).isEqualTo("Child 1-1")
        assertThat(result[0].children[1].title).isEqualTo("Child 1-2")
        assertThat(result[0].children[2].title).isEqualTo("Child 1-3")
        assertThat(result[0].children[3].title).isEqualTo("Child 2-1")
        assertThat(result[1].title).isEqualTo("Child 1-1")
        assertThat(result[2].title).isEqualTo("Child 1-2")
        assertThat(result[3].title).isEqualTo("Child 1-3")
        assertThat(result[4].title).isEqualTo("Child 2-1")
        assertThat(result[5].title).isEqualTo("Item 2")
        assertThat(result[5].children[0].title).isEqualTo("Child 2-2")
        assertThat(result[5].children[1].title).isEqualTo("Child 2-3")
        assertThat(result[6].title).isEqualTo("Child 2-2")
        assertThat(result[7].title).isEqualTo("Child 2-3")
    }

    @Test
    fun `when child is moved below another parent, then should move the child in local and global lists`() {
        // when
        val result = withUpdatedPosition(
            initialCheckboxes,
            ItemPosition(0, initialCheckboxes[3]),
            ItemPosition(0, initialCheckboxes[4])
        )

        // then
        assertThat(result[0].title).isEqualTo("Item 1")
        assertThat(result[0].children[0].title).isEqualTo("Child 1-1")
        assertThat(result[0].children[1].title).isEqualTo("Child 1-2")
        assertThat(result[1].title).isEqualTo("Child 1-1")
        assertThat(result[2].title).isEqualTo("Child 1-2")
        assertThat(result[3].title).isEqualTo("Item 2")
        assertThat(result[3].children[0].title).isEqualTo("Child 1-3")
        assertThat(result[3].children[1].title).isEqualTo("Child 2-1")
        assertThat(result[3].children[2].title).isEqualTo("Child 2-2")
        assertThat(result[3].children[3].title).isEqualTo("Child 2-3")
        assertThat(result[4].title).isEqualTo("Child 1-3")
        assertThat(result[5].title).isEqualTo("Child 2-1")
        assertThat(result[6].title).isEqualTo("Child 2-2")
        assertThat(result[7].title).isEqualTo("Child 2-3")
    }

    @Test
    fun `should not crash in this special case`() {
        val from = ViewTemplateCheckbox.New(
            id = TemplateCheckboxId(id = 4),
            parentId = null,
            title = "Item 2",
            children = listOf(
                ViewTemplateCheckbox.New(
                    id = TemplateCheckboxId(id = 6),
                    parentId = TemplateCheckboxId(id = 4),
                    title = "Child 2-2",
                    children = listOf()
                ), ViewTemplateCheckbox.New(
                    id = TemplateCheckboxId(id = 7),
                    parentId = TemplateCheckboxId(id = 4),
                    title = "Child 2-3",
                    children = listOf()
                ), ViewTemplateCheckbox.New(
                    id = TemplateCheckboxId(id = 2),
                    parentId = TemplateCheckboxId(id = 4),
                    title = "Child 1-2",
                    children = listOf()
                ), ViewTemplateCheckbox.New(
                    id = TemplateCheckboxId(id = 3),
                    parentId = TemplateCheckboxId(id = 4),
                    title = "Child 1-3",
                    children = listOf()
                )
            )
        )
        val to = ViewTemplateCheckbox.New(
            id = TemplateCheckboxId(id = 0),
            parentId = null,
            title = "Item 1",
            children = listOf(
                ViewTemplateCheckbox.New(
                    id = TemplateCheckboxId(id = 1),
                    parentId = TemplateCheckboxId(id = 0),
                    title = "Child 1-1",
                    children = listOf()
                ),
                ViewTemplateCheckbox.New(
                    id = TemplateCheckboxId(id = 5),
                    parentId = TemplateCheckboxId(id = 0),
                    title = "Child 2-1",
                    children = listOf()
                )
            )
        )

        val checkboxes = listOf(from, to).flatMap {
            listOf(it) + it.children
        }

        // when
        val result = withUpdatedPosition(
            checkboxes,
            ItemPosition(0, from),
            ItemPosition(0, to)
        )

        // then
        assertThat(result[0].title).isEqualTo("Item 1")
        assertThat(result[0].children[0].title).isEqualTo("Child 1-1")
        assertThat(result[0].children[1].title).isEqualTo("Child 2-1")
        assertThat(result[1].title).isEqualTo("Child 1-1")
        assertThat(result[2].title).isEqualTo("Child 2-1")
        assertThat(result[3].title).isEqualTo("Item 2")
        assertThat(result[3].children[0].title).isEqualTo("Child 2-2")
        assertThat(result[3].children[1].title).isEqualTo("Child 2-3")
        assertThat(result[3].children[2].title).isEqualTo("Child 1-2")
        assertThat(result[3].children[3].title).isEqualTo("Child 1-3")
        assertThat(result[4].title).isEqualTo("Child 2-2")
        assertThat(result[5].title).isEqualTo("Child 2-3")
        assertThat(result[6].title).isEqualTo("Child 1-2")
        assertThat(result[7].title).isEqualTo("Child 1-3")


//        ClipData.Item 2
//        |       Child 2-2
//        |       Child 2-3
//        |       Child 1-2
//        |       Child 1-3
//        Child 2-2
//
//        Child 2-3
//
//        Child 1-2
//
//        Child 1-3
//
//        ClipData.Item 1
//        |       Child 1-1
//        |       Child 2-1
//        Child 1-1
//
//        Child 2-1
    }
}
