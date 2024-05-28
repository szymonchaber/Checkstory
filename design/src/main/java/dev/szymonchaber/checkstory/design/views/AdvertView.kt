package dev.szymonchaber.checkstory.design.views

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError
import com.google.firebase.crashlytics.FirebaseCrashlytics
import dev.szymonchaber.checkstory.design.ActiveUser
import dev.szymonchaber.checkstory.design.BuildConfig
import timber.log.Timber

private val dividerColor = Color.Black.copy(alpha = 0.5f)

@SuppressLint("MissingPermission")
@Composable
fun AdvertView(modifier: Modifier = Modifier) {
    var adLoadingFailed by remember {
        mutableStateOf(false)
    }
    if (!adLoadingFailed) {
        Column(Modifier.background(Color.White)) {
            Spacer(
                modifier = Modifier
                    .height(2.dp)
                    .fillMaxWidth()
                    .background(dividerColor)
            )
            Box(
                modifier = Modifier
                    .height(50.dp)
            ) {
                val isInEditMode = LocalInspectionMode.current
                if (isInEditMode) {
                    Text(
                        modifier = modifier
                            .fillMaxWidth()
                            .background(Color.Red)
                            .padding(horizontal = 2.dp, vertical = 6.dp),
                        textAlign = TextAlign.Center,
                        color = Color.White,
                        text = "Advert Here",
                    )
                } else {
                    var shouldShowLoading by remember {
                        mutableStateOf(true)
                    }
                    if (shouldShowLoading) {
                        LoadingViewNoPadding()
                    }
                    AndroidView(
                        modifier = modifier.fillMaxWidth(),
                        factory = { context ->
                            AdView(context).apply {
                                setAdSize(AdSize.BANNER)
                                adUnitId = BuildConfig.BANNER_AD_UNIT_ID
                                adListener = object : AdListener() {

                                    override fun onAdLoaded() {
                                        shouldShowLoading = false
                                    }

                                    override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                                        Timber.e(loadAdError.toString())
                                        FirebaseCrashlytics.getInstance()
                                            .recordException(Exception("Loading an ad failed! $loadAdError"))
                                        shouldShowLoading = false
                                        adLoadingFailed = true
                                    }
                                }
                                loadAd(AdRequest.Builder().build())
                            }
                        }
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AdvertPreview() {
    AdvertView()
}

@Composable
fun AdvertScaffold(
    topBar: @Composable () -> Unit,
    content: @Composable (PaddingValues) -> Unit,
    floatingActionButton: @Composable () -> Unit = {},
    backgroundColor: Color = MaterialTheme.colors.background,
) {
    val activeUser = ActiveUser.current
    Scaffold(
        backgroundColor = backgroundColor,
        topBar = topBar,
        content = {
            Box(
                Modifier
                    .fillMaxSize()
                    .padding(it)
            ) {
                content(it)
                if (activeUser.isPaidUser.not()) {
                    Text(
                        modifier = Modifier
                            .background(dividerColor, RoundedCornerShape(topEnd = 4.dp))
                            .padding(horizontal = 8.dp)
                            .align(Alignment.BottomStart),
                        color = Color.White,
                        text = "ad"
                    )
                }
            }
        },
        floatingActionButton = floatingActionButton,
        bottomBar = {
            if (activeUser.isPaidUser.not()) {
                AdvertView()
            }
        }
    )
}
