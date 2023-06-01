package dev.szymonchaber.checkstory.data.migration

import com.google.common.truth.Truth.assertThat
import com.google.firebase.crashlytics.FirebaseCrashlytics
import dev.szymonchaber.checkstory.domain.model.ChecklistCommand
import dev.szymonchaber.checkstory.domain.model.TemplateCommand
import dev.szymonchaber.checkstory.domain.model.checklist.template.ChecklistTemplate
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

internal class CommandModelMigrationTest {

    private val migrationPreferences = mock<MigrationPreferences>()

    private val templateRepository = mock<ChecklistTemplateRepository> {
        on { getAll() } doReturn flowOf(listOf())
    }

    private val synchronizer = mock<Synchronizer>()

    private val firebaseCrashlytics = mock<FirebaseCrashlytics>()

    private val migration = CommandModelMigration(
        migrationPreferences,
        templateRepository,
        synchronizer,
        firebaseCrashlytics
    )

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
                assertThat(it[0]).isEqualTo(
                    TemplateCommand.CreateNewTemplate(
                        templates.first().id,
                        templates.first().createdAt.toKotlinLocalDateTime()
                            .toInstant(TimeZone.currentSystemDefault()),
                        it[0].commandId,
                        templates.first(),
                    )
                )
                assertThat(it[1]).isEqualTo(
                    ChecklistCommand.CreateChecklistCommand(
                        templates.first().checklists[0].id,
                        templates.first().checklists[0].checklistTemplateId,
                        templates.first().checklists[0].title,
                        templates.first().checklists[0].description,
                        templates.first().checklists[0].items,
                        it[1].commandId,
                        templates.first().checklists[0].createdAt.toKotlinLocalDateTime()
                            .toInstant(TimeZone.currentSystemDefault()),
                        templates.first().checklists[0].notes,
                    )
                )
                assertThat(it[2]).isEqualTo(
                    ChecklistCommand.CreateChecklistCommand(
                        templates.first().checklists[1].id,
                        templates.first().checklists[1].checklistTemplateId,
                        templates.first().checklists[1].title,
                        templates.first().checklists[1].description,
                        templates.first().checklists[1].items,
                        it[2].commandId,
                        templates.first().checklists[1].createdAt.toKotlinLocalDateTime()
                            .toInstant(TimeZone.currentSystemDefault()),
                        templates.first().checklists[1].notes,
                    )
                )
            })
        }

    private fun checklistTemplates(): List<ChecklistTemplate> {
        return listOf(ChecklistTestUtils.createChecklistTemplate())
    }
}
