package dev.szymonchaber.checkstory.payments

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import dev.szymonchaber.checkstory.common.trackScreenName
import dev.szymonchaber.checkstory.design.R
import dev.szymonchaber.checkstory.payments.components.MainPaymentButton
import nl.dionsegijn.konfetti.compose.KonfettiView
import nl.dionsegijn.konfetti.core.Party
import nl.dionsegijn.konfetti.core.Position
import nl.dionsegijn.konfetti.core.emitter.Emitter
import nl.dionsegijn.konfetti.core.models.Shape

@Composable
@Destination<PaymentGraph>(route = "payment_success_screen")
fun PaymentSuccessScreen(
    navigator: DestinationsNavigator
) {
    trackScreenName("payment_success")
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(text = stringResource(R.string.upgrade))
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            navigator.navigateUp()
                        }
                    ) {
                        Icon(Icons.Filled.ArrowBack, "")
                    }
                },
                elevation = 12.dp
            )
        }, content = {
            Box(modifier = Modifier.padding(it)) {
                SubscriptionSuccessView()
            }
        },
        bottomBar = {
            Column(Modifier.fillMaxWidth()) {
                MainPaymentButton({ navigator.navigateUp() }) {
                    Text(text = stringResource(id = R.string.payment_continue))
                }
            }
        }
    )
}

@Composable
private fun SubscriptionSuccessView() {
    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .clip(RoundedCornerShape(12.dp))
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFFD6CFFC),
                            Color(0xFFFDF9D8)
                        ),
                    )
                )
                .padding(30.dp)
        ) {
            Text(
                color = MaterialTheme.colors.onSecondary,
                fontWeight = FontWeight.Medium,
                text = stringResource(id = R.string.subscription_successful_thanks_for_support)
            )
        }
        KonfettiView(
            modifier = Modifier.fillMaxSize(),
            parties = listOf(
                party(0.0, 300),
                party(1.0, 240),
            ),
        )
    }
}

@Composable
private fun party(xRelative: Double, angle: Int) = Party(
    emitter = Emitter(duration = 200).perSecond(300),
    position = Position.Relative(xRelative, 0.7),
    spread = 40,
    angle = angle,
    delay = 500,
    speed = 60f,
    shapes = listOf(Shape.Square, Shape.Circle)
)
