package dev.szymonchaber.checkstory.payments.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.szymonchaber.checkstory.payments.billing.SubscriptionPlan
import dev.szymonchaber.checkstory.payments.billing.SubscriptionPlans

@Composable
internal fun PaymentsPlans(
    subscriptionPlans: SubscriptionPlans,
    selectedPlan: SubscriptionPlan?,
    onPlanSelected: (SubscriptionPlan) -> Unit
) {
    Row(
        modifier = Modifier
            .padding(horizontal = 20.dp)
            .padding(top = 24.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        with(subscriptionPlans) {
            SubscriptionPlanView(monthly, monthly == selectedPlan, onPlanSelected)
            SubscriptionPlanView(yearly, yearly == selectedPlan, onPlanSelected)
            SubscriptionPlanView(quarterly, quarterly == selectedPlan, onPlanSelected)
        }
    }
}
