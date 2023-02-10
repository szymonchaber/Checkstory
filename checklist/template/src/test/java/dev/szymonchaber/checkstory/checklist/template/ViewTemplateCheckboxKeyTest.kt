package dev.szymonchaber.checkstory.checklist.template

import com.google.common.truth.Truth.assertThat
import org.junit.Test


internal class ViewTemplateCheckboxKeyTest {

    @Test
    fun `given the checkbox has no ancestors, then should return nestingLevel of 1`() {
        // given
        val levelOne = ViewTemplateCheckboxKey(viewId = 1, parentKey = null, isNew = true, isParent = true)

        // when
        val nestingLevel = levelOne.nestingLevel

        // then
        assertThat(nestingLevel).isEqualTo(1)
    }

    @Test
    fun `given the checkbox has one ancestor, then should return nestingLevel of 2`() {
        // given
        val levelOne = ViewTemplateCheckboxKey(viewId = 1, parentKey = null, isNew = true, isParent = true)
        val levelTwo = ViewTemplateCheckboxKey(viewId = 1, parentKey = levelOne, isNew = true, isParent = true)

        // when
        val nestingLevel = levelTwo.nestingLevel

        // then
        assertThat(nestingLevel).isEqualTo(2)
    }

    @Test
    fun `given the checkbox has two ancestors, then should return nestingLevel of 3`() {
        // given
        val levelOne = ViewTemplateCheckboxKey(viewId = 1, parentKey = null, isNew = true, isParent = true)
        val levelTwo = ViewTemplateCheckboxKey(viewId = 1, parentKey = levelOne, isNew = true, isParent = true)
        val levelThree = ViewTemplateCheckboxKey(viewId = 1, parentKey = levelTwo, isNew = true, isParent = true)

        // when
        val nestingLevel = levelThree.nestingLevel

        // then
        assertThat(nestingLevel).isEqualTo(3)
    }

    @Test
    fun `given the checkbox has three ancestors, then should return nestingLevel of 4`() {
        // given
        val levelOne = ViewTemplateCheckboxKey(viewId = 1, parentKey = null, isNew = true, isParent = true)
        val levelTwo = ViewTemplateCheckboxKey(viewId = 1, parentKey = levelOne, isNew = true, isParent = true)
        val levelThree = ViewTemplateCheckboxKey(viewId = 1, parentKey = levelTwo, isNew = true, isParent = true)
        val levelFour = ViewTemplateCheckboxKey(viewId = 1, parentKey = levelThree, isNew = true, isParent = true)

        // when
        val nestingLevel = levelFour.nestingLevel

        // then
        assertThat(nestingLevel).isEqualTo(4)
    }

    @Test
    fun `given the checkbox has four ancestors, then should return nestingLevel of 5`() {
        // given
        val levelOne = ViewTemplateCheckboxKey(viewId = 1, parentKey = null, isNew = true, isParent = true)
        val levelTwo = ViewTemplateCheckboxKey(viewId = 1, parentKey = levelOne, isNew = true, isParent = true)
        val levelThree = ViewTemplateCheckboxKey(viewId = 1, parentKey = levelTwo, isNew = true, isParent = true)
        val levelFour = ViewTemplateCheckboxKey(viewId = 1, parentKey = levelThree, isNew = true, isParent = true)
        val levelFive = ViewTemplateCheckboxKey(viewId = 1, parentKey = levelFour, isNew = true, isParent = true)

        // when
        val nestingLevel = levelFive.nestingLevel

        // then
        assertThat(nestingLevel).isEqualTo(5)
    }
}
