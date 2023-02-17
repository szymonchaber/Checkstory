package dev.szymonchaber.checkstory.onboarding

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import dev.szymonchaber.checkstory.common.trackScreenName
import dev.szymonchaber.checkstory.navigation.Routes
import kotlinx.coroutines.launch

@Destination("onboarding_screen", start = true)
@Composable
fun OnboardingScreen(
    navigator: DestinationsNavigator
) {
    trackScreenName("onboarding_screen")

    BackHandler {
    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(text = stringResource(R.string.checkstory))
                },
                elevation = 12.dp
            )
        },
        content = {
            Box(
                Modifier.padding(it)
            ) {
                OnboardingView(navigator)
            }
        }
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingView(navigator: DestinationsNavigator) {
    val pagerState = rememberPagerState()
    val scope = rememberCoroutineScope()
    HorizontalPager(pageCount = 2, state = pagerState, userScrollEnabled = false) { page ->
        when (page) {
            0 -> ReusableChecklistsPage {
                scope.launch {
                    pagerState.animateScrollToPage(1)
                }
            }
            1 -> KeepTrackOfThePastPage {
                navigator.navigate(Routes.aboutScreen()) {
                    popUpTo(Routes.homeScreen())
                }
            }
        }
    }
}

@Composable
private fun ReusableChecklistsPage(onButtonClick: () -> Unit) {
    TextButton(onClick = onButtonClick) {
        Text(text = "Next!")
    }
}

@Composable
private fun KeepTrackOfThePastPage(onButtonClick: () -> Unit) {
    TextButton(onClick = onButtonClick) {
        Text(text = "Let's begin!")
    }
}
