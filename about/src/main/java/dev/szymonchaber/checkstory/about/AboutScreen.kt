package dev.szymonchaber.checkstory.about

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.ExternalModuleGraph
import com.ramcosta.composedestinations.annotation.NavGraph
import com.ramcosta.composedestinations.annotation.parameters.DeepLink
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import dev.szymonchaber.checkstory.common.trackScreenName
import dev.szymonchaber.checkstory.design.R
import dev.szymonchaber.checkstory.design.views.AdvertScaffold

@NavGraph<ExternalModuleGraph>
annotation class AboutGraph

@SuppressLint("MissingPermission")
@Destination<AboutGraph>(
    route = "about_screen",
    start = true,
    deepLinks = [
        DeepLink(
            uriPattern = "app://checkstory/about"
        ),
    ]
)
@Composable
fun AboutScreen(
    navigator: DestinationsNavigator
) {
    trackScreenName("about")
    AdvertScaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(text = stringResource(R.string.about))
                },
                navigationIcon = {
                    IconButton(onClick = {
                        navigator.navigateUp()
                    }) {
                        Icon(Icons.Filled.ArrowBack, "")
                    }
                },
                elevation = 12.dp,
            )
        },
        content = {
            val uriHandler = LocalUriHandler.current
            val context = LocalContext.current
            val versionName = remember {
                try {
                    val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
                    packageInfo.versionName
                } catch (nameNotFoundException: PackageManager.NameNotFoundException) {
                    FirebaseCrashlytics.getInstance().recordException(nameNotFoundException)
                    null
                }
            }
            AboutView("v$versionName",
                onEmailClick = {
                    FirebaseAnalytics.getInstance(context).logEvent("about_email_clicked", null)
                    composeEmail(
                        context, "szymon@szymonchaber.dev", "Just reaching out about Checkstory",
                        "Hello Szymon, I wanted to reach out with a question / feedback / praise / an improvement idea!\n\n\n\n\n\n\nApp version: $versionName"
                    )
                }, onTwitterClick = {
                    FirebaseAnalytics.getInstance(context).logEvent("about_twitter_clicked", null)
                    uriHandler.openUri("https://twitter.com/SzymonChaber")
                })
        },
    )
}

@Composable
@Preview(showBackground = true)
fun AboutView(appVersion: String? = "1.0.0", onEmailClick: () -> Unit = {}, onTwitterClick: () -> Unit = {}) {
    Box(
        Modifier
            .fillMaxHeight()
            .padding(16.dp)
    ) {
        Column(Modifier.align(Alignment.TopCenter)) {
            val email = stringResource(id = R.string.about_email)
            TextWithUnderlinedSection(
                text = stringResource(id = R.string.about_first_paragraph) + email,
                partToUnderline = email,
                onClick = onEmailClick
            )
            val twitter = stringResource(id = R.string.about_twitter)
            TextWithUnderlinedSection(
                modifier = Modifier.padding(top = 8.dp),
                text = stringResource(id = R.string.about_second_paragraph) + twitter,
                partToUnderline = twitter,
                onClick = onTwitterClick
            )
        }
        appVersion?.let {
            Text(
                modifier = Modifier
                    .align(Alignment.BottomCenter),
                text = it
            )
        }
    }
}

fun composeEmail(context: Context, address: String, subject: String, body: String) {
    val intent = Intent(Intent.ACTION_SENDTO)
    intent.data = Uri.parse("mailto:")
    intent.putExtra(Intent.EXTRA_EMAIL, arrayOf(address))
    intent.putExtra(Intent.EXTRA_SUBJECT, subject)
    intent.putExtra(Intent.EXTRA_TEXT, body)
    try {
        context.startActivity(intent)
    } catch (exception: ActivityNotFoundException) {
        FirebaseCrashlytics.getInstance().recordException(exception)
    }
}

@Composable
private fun TextWithUnderlinedSection(
    modifier: Modifier = Modifier,
    text: String,
    partToUnderline: String,
    onClick: () -> Unit
) {
    val annotatedString = buildAnnotatedString {
        append(text)
        val indexOfLink = text.indexOf(partToUnderline)
        val endIndexOfLink = indexOfLink + partToUnderline.length
        addStyle(
            style = SpanStyle(
                textDecoration = TextDecoration.Underline
            ),
            start = indexOfLink,
            end = endIndexOfLink
        )
    }

    ClickableText(
        modifier = modifier,
        text = annotatedString,
        style = LocalTextStyle.current, onClick = { onClick() }
    )
}
