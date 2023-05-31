package dev.szymonchaber.checkstory.data.migration

import com.google.common.truth.Truth.assertThat
import com.google.firebase.crashlytics.FirebaseCrashlytics
import dev.szymonchaber.checkstory.domain.model.ChecklistCommand
import dev.szymonchaber.checkstory.domain.model.TemplateCommand
import dev.szymonchaber.checkstory.domain.model.checklist.fill.Checkbox
import dev.szymonchaber.checkstory.domain.model.checklist.fill.CheckboxId
import dev.szymonchaber.checkstory.domain.model.checklist.fill.Checklist
import dev.szymonchaber.checkstory.domain.model.checklist.fill.ChecklistId
import dev.szymonchaber.checkstory.domain.model.checklist.template.ChecklistTemplate
import dev.szymonchaber.checkstory.domain.model.checklist.template.ChecklistTemplateId
import dev.szymonchaber.checkstory.domain.model.checklist.template.TemplateCheckbox
import dev.szymonchaber.checkstory.domain.model.checklist.template.TemplateCheckboxId
import dev.szymonchaber.checkstory.domain.model.checklist.template.reminder.Reminder
import dev.szymonchaber.checkstory.domain.model.checklist.template.reminder.ReminderId
import dev.szymonchaber.checkstory.domain.repository.ChecklistTemplateRepository
import dev.szymonchaber.checkstory.domain.repository.Synchronizer
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toKotlinLocalDateTime
import org.junit.Test
import org.mockito.kotlin.check
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.isA
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.verifyNoMoreInteractions
import org.mockito.kotlin.whenever
import java.time.LocalDateTime
import java.util.*

internal class CommandModelMigrationTest {

    private val migrationPreferences = mock<MigrationPreferences>()

    private val templateRepository = mock<ChecklistTemplateRepository> {
        on { getAll() } doReturn flowOf(listOf())
    }

    private val synchronizer = mock<Synchronizer>()

    private val firebaseCrashlytics = mock<FirebaseCrashlytics>()

    private val staticCommandUuid = UUID.randomUUID()

    private val migration = CommandModelMigration(
        migrationPreferences,
        templateRepository,
        synchronizer,
        firebaseCrashlytics
    ) {
        staticCommandUuid
    }

    @Test
    fun `given migration did run, when run called, then should do nothing`() = runBlocking {
        // given
        whenever(migrationPreferences.didRunCommandModelMigration()) doReturn true

        // when
        migration.run()

        // then
        verifyNoInteractions(synchronizer)
        verifyNoInteractions(templateRepository)
    }

    @Test
    fun `given migration did not run but commands present, when run called, then should only log exception`() =
        runBlocking {
            // given
            whenever(migrationPreferences.didRunCommandModelMigration()) doReturn false
            whenever(synchronizer.hasUnsynchronizedCommands()) doReturn true

            // when
            migration.run()

            // then
            verify(firebaseCrashlytics).recordException(
                isA<CommandMigrationAttemptedWithCommandsExisting>()
            )
            verifyNoInteractions(templateRepository)
        }

    @Test
    fun `given migration did not run, when run called, then should mark migration did run`() =
        runBlocking {
            // given
            whenever(migrationPreferences.didRunCommandModelMigration()) doReturn false
            whenever(synchronizer.hasUnsynchronizedCommands()) doReturn false

            // when
            migration.run()

            // then
            verify(migrationPreferences).markDidRunCommandModelMigration()
        }

    @Test
    fun `given no data is present, when run called, then should not call synchronized`() =
        runBlocking {
            // given
            whenever(migrationPreferences.didRunCommandModelMigration()) doReturn false
            whenever(synchronizer.hasUnsynchronizedCommands()) doReturn false

            // when
            migration.run()

            // then
            verify(synchronizer).hasUnsynchronizedCommands()
            verifyNoMoreInteractions(synchronizer)
        }

    @Test
    fun `given data present, when run called, then should synchronize commands made from existing data`() =
        runBlocking {
            // given
            whenever(migrationPreferences.didRunCommandModelMigration()) doReturn false
            whenever(synchronizer.hasUnsynchronizedCommands()) doReturn false
            val templates = checklistTemplates()
            whenever(templateRepository.getAll()) doReturn flowOf(templates)

            // when
            migration.run()

            // then
            verify(synchronizer).synchronizeCommands(check {
                assertThat(it).containsExactly(
                    TemplateCommand.CreateNewTemplate(
                        templates.first().id,
                        templates.first().createdAt.toKotlinLocalDateTime().toInstant(TimeZone.currentSystemDefault()),
                        staticCommandUuid,
                        templates.first(),
                    ),
                    ChecklistCommand.CreateChecklistCommand(
                        templates.first().checklists[0].id,
                        templates.first().checklists[0].checklistTemplateId,
                        templates.first().checklists[0].title,
                        templates.first().checklists[0].description,
                        templates.first().checklists[0].items,
                        staticCommandUuid,
                        templates.first().checklists[0].createdAt.toKotlinLocalDateTime()
                            .toInstant(TimeZone.currentSystemDefault()),
                        templates.first().checklists[0].notes,
                    ),
                    ChecklistCommand.CreateChecklistCommand(
                        templates.first().checklists[1].id,
                        templates.first().checklists[1].checklistTemplateId,
                        templates.first().checklists[1].title,
                        templates.first().checklists[1].description,
                        templates.first().checklists[1].items,
                        staticCommandUuid,
                        templates.first().checklists[1].createdAt.toKotlinLocalDateTime()
                            .toInstant(TimeZone.currentSystemDefault()),
                        templates.first().checklists[1].notes,
                    )
                ).inOrder()
            })
        }

    private fun checklistTemplates(): List<ChecklistTemplate> {
        return listOf(ChecklistTestUtils.createChecklistTemplate())
    }
}

object ChecklistTestUtils {

    fun createChecklistTemplate(): ChecklistTemplate {
        val templateId = ChecklistTemplateId.new()
        val templateCheckboxId = TemplateCheckboxId(UUID.randomUUID())
        val templateCheckboxId2 = TemplateCheckboxId(UUID.randomUUID())
        return ChecklistTemplate(
            id = templateId,
            title = "Example template",
            description = "Example template description",
            items = listOf(
                TemplateCheckbox(
                    id = templateCheckboxId,
                    parentId = null,
                    title = "Top task 0",
                    children = listOf(
                        TemplateCheckbox(
                            id = TemplateCheckboxId(UUID.randomUUID()),
                            parentId = templateCheckboxId,
                            title = "CHILD_TASK_0",
                            children = listOf(),
                            sortPosition = 0,
                            templateId = templateId
                        ),
                        TemplateCheckbox(
                            id = TemplateCheckboxId(UUID.randomUUID()),
                            parentId = templateCheckboxId,
                            title = "CHILD_TASK_1",
                            children = listOf(),
                            sortPosition = 1,
                            templateId = templateId
                        )
                    ),
                    sortPosition = 0,
                    templateId = templateId
                ),
                TemplateCheckbox(
                    id = templateCheckboxId2,
                    parentId = null,
                    title = "Top task 1",
                    children = listOf(
                        TemplateCheckbox(
                            id = TemplateCheckboxId(UUID.randomUUID()),
                            parentId = templateCheckboxId2,
                            title = "CHILD_TASK_2",
                            children = listOf(),
                            sortPosition = 0,
                            templateId = templateId
                        ),
                        TemplateCheckbox(
                            id = TemplateCheckboxId(UUID.randomUUID()),
                            parentId = templateCheckboxId2,
                            title = "CHILD_TASK_3",
                            children = listOf(),
                            sortPosition = 1,
                            templateId = templateId
                        )
                    ),
                    sortPosition = 1,
                    templateId = templateId
                )
            ),
            createdAt = LocalDateTime.now(),
            checklists = listOf(checklist(templateId), checklist(templateId)),
            reminders = listOf(reminderExact(templateId))
        )
    }

    private fun reminderExact(templateId: ChecklistTemplateId): Reminder {
        return Reminder.Exact(
            id = ReminderId(UUID.randomUUID()),
            forTemplate = templateId,
            startDateTime = LocalDateTime.now().plusDays(1)
        )
    }

    private fun checklist(templateId: ChecklistTemplateId): Checklist {
        val checklistId = ChecklistId(UUID.randomUUID())
        return Checklist(
            id = checklistId,
            checklistTemplateId = templateId,
            title = "Example checklist",
            description = "Example checklist description",
            items = List(5) { checkbox(checklistId, it) },
            notes = "Example notes",
            createdAt = LocalDateTime.now()
        )
    }

    private fun checkbox(checklistId: ChecklistId, index: Int): Checkbox {
        val parentId = CheckboxId(UUID.randomUUID())
        return Checkbox(
            id = parentId,
            parentId = null,
            checklistId = checklistId,
            title = "Example item $index",
            isChecked = false,
            children = listOf(
                Checkbox(
                    id = CheckboxId(UUID.randomUUID()),
                    parentId = parentId,
                    checklistId = checklistId,
                    title = "Example item child item 0",
                    isChecked = false,
                    children = emptyList()
                ),
                Checkbox(
                    id = CheckboxId(UUID.randomUUID()),
                    parentId = parentId,
                    checklistId = checklistId,
                    title = "Example item child item 1",
                    isChecked = false,
                    children = emptyList()
                )
            )
        )
    }
}
