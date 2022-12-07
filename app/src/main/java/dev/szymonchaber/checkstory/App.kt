package dev.szymonchaber.checkstory

import android.app.AlarmManager
import android.app.Application
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.HiltAndroidApp
import dev.szymonchaber.checkstory.data.preferences.OnboardingPreferences
import dev.szymonchaber.checkstory.domain.model.checklist.template.ChecklistTemplate
import dev.szymonchaber.checkstory.domain.model.checklist.template.ChecklistTemplateId
import dev.szymonchaber.checkstory.domain.model.checklist.template.TemplateCheckbox
import dev.szymonchaber.checkstory.domain.model.checklist.template.TemplateCheckboxId
import dev.szymonchaber.checkstory.domain.usecase.IsProUserUseCase
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
    lateinit var isProUserUseCase: IsProUserUseCase

    @Inject
    lateinit var onboardingPreferences: OnboardingPreferences

    private val alarmManager by lazy { getSystemService(Context.ALARM_SERVICE) as AlarmManager }

    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
        setPaymentTierProperty()
        runReminderSchedulerDaily()
        insertOnboardingTemplates()
        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) {
                Timber.w("Fetching FCM registration token failed", task.exception)
                return@OnCompleteListener
            }
            Timber.d("Token: ${task.result}")
        })
    }

    private fun setPaymentTierProperty() {
        GlobalScope.launch {
            withContext(Dispatchers.IO) {
                val tier = if (isProUserUseCase.isProUser()) SUBSCRIPTION_TIER_PRO else SUBSCRIPTION_TIER_FREE
                FirebaseAnalytics.getInstance(this@App).setUserProperty(SUBSCRIPTION_TIER, tier)
            }
        }
    }

    private fun insertOnboardingTemplates() {
        if (onboardingPreferences.didGenerateOnboardingTemplate) {
            return
        }
        val checkboxes = checkboxes {
            resources.getStringArray(R.array.onboarding).map { section ->
                val sections = section.split("|")
                val main = sections.first()
                checkbox(main) {
                    sections.drop(1).forEach(this::childCheckbox)
                }
            }
        }
        GlobalScope.launch {
            withContext(Dispatchers.IO) {
                updateChecklistTemplateUseCase.updateChecklistTemplate(
                    ChecklistTemplate(
                        ChecklistTemplateId(0),
                        resources.getString(R.string.onboarding_template_title),
                        resources.getString(R.string.onboarding_template_description),
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

            fun childCheckbox(title: String) {
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

    companion object {

        private const val SUBSCRIPTION_TIER = "subscription_tier"
        private const val SUBSCRIPTION_TIER_FREE = "free"
        private const val SUBSCRIPTION_TIER_PRO = "pro"
    }
}
