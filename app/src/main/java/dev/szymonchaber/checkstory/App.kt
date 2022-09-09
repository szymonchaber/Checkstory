package dev.szymonchaber.checkstory

import android.app.AlarmManager
import android.app.Application
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import dagger.hilt.android.HiltAndroidApp
import dev.szymonchaber.checkstory.data.preferences.OnboardingPreferences
import dev.szymonchaber.checkstory.domain.model.checklist.template.ChecklistTemplate
import dev.szymonchaber.checkstory.domain.model.checklist.template.ChecklistTemplateId
import dev.szymonchaber.checkstory.domain.model.checklist.template.TemplateCheckbox
import dev.szymonchaber.checkstory.domain.model.checklist.template.TemplateCheckboxId
import dev.szymonchaber.checkstory.domain.usecase.UpdateChecklistTemplateUseCase
import dev.szymonchaber.checkstory.notifications.ReminderScheduler
import dev.szymonchaber.checkstory.notifications.ScheduleTodayRemindersReceiver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import javax.inject.Inject


@HiltAndroidApp
class App : Application() {

    @Inject
    lateinit var reminderScheduler: ReminderScheduler

    @Inject
    lateinit var updateChecklistTemplateUseCase: UpdateChecklistTemplateUseCase

    @Inject
    lateinit var onboardingPreferences: OnboardingPreferences

    private val alarmManager by lazy { getSystemService(Context.ALARM_SERVICE) as AlarmManager }

    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
        runReminderSchedulerDaily()
        insertOnboardingTemplates()
    }

    private fun insertOnboardingTemplates() {
        if (onboardingPreferences.didGenerateOnboardingTemplate) {
            return
        }
        val checkboxes = checkboxes {
            checkbox("You can add as many items as you want") {
                checkbox("You can also nest them!")
            }
            checkbox("Below you can set reminders, so you get notified to start a checklist at the right time") {}
            checkbox("Every template has a history of its checklists") {
                checkbox("You can access it from the main screen (calendar icon)")
                checkbox("There, you can see which items were done")
                checkbox("and what notes you had")
            }
            checkbox("Editing a template does not rewrite existing checklists") {
                checkbox("History is not rewritten")
                checkbox("You can see how your checklist changed over time")
            }
            checkbox("In the free version, you can have:") {
                checkbox("3 templates")
                checkbox("20 checklists per template")
                checkbox("3 reminders per template")
            }
            checkbox("It’s all unlimited in the pro version ") {}
            checkbox("Now delete this template, so you don’t use up your free tier") {}
            checkbox("Start using Checkstory and Never Forget A Step Again ✅") {}
        }
        GlobalScope.launch {
            withContext(Dispatchers.IO) {
                updateChecklistTemplateUseCase.updateChecklistTemplate(
                    ChecklistTemplate(
                        ChecklistTemplateId(0),
                        "(Click the pencil) This is your template!",
                        "Templates are used to quickly create the same checklist again and again.\n" +
                                "No clearing of the old ones required",
                        checkboxes,
                        LocalDateTime.now(),
                        listOf(),
                        listOf()
                    )
                )
                onboardingPreferences.didGenerateOnboardingTemplate = true
            }
        }
    }

    class CheckboxesScope {

        val checkboxes = mutableMapOf<String, MutableList<String>>()

        inner class ChildScope(private val parent: String) {

            fun checkbox(title: String) {
                checkboxes[parent] = checkboxes[parent]?.plus(title)?.toMutableList() ?: mutableListOf(title)
            }
        }

        fun checkbox(title: String, children: ChildScope.() -> Unit) {
            checkboxes[title] = mutableListOf()
            ChildScope(title).children()
        }
    }

    private fun checkboxes(block: CheckboxesScope.() -> Unit): List<TemplateCheckbox> {
        val checkboxesScope = CheckboxesScope()
        checkboxesScope.block()
        return checkboxesScope.checkboxes.map { (title, children) ->
            TemplateCheckbox(TemplateCheckboxId(0), null, title, children.map {
                TemplateCheckbox(TemplateCheckboxId(0), null, it, listOf())
            })
        }
    }

    private fun runReminderSchedulerDaily() {
        alarmManager.setRepeating(
            AlarmManager.RTC_WAKEUP,
            LocalDateTime.now()
                .withHour(23)
                .withMinute(59)
                .toInstant(ZoneId.systemDefault().rules.getOffset(Instant.now()))
                .toEpochMilli(),
            AlarmManager.INTERVAL_DAY,
            createIntent()
        )
    }

    private fun createIntent(): PendingIntent {
        return Intent(this, ScheduleTodayRemindersReceiver::class.java)
            .let {
                PendingIntent.getBroadcast(
                    this,
                    1000,
                    it,
                    PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
            }
    }
}
