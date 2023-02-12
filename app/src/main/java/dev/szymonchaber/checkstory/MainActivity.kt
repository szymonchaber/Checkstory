package dev.szymonchaber.checkstory

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.net.toUri
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.gms.ads.MobileAds
import com.google.android.play.core.review.ReviewManagerFactory
import dagger.hilt.android.AndroidEntryPoint
import dev.szymonchaber.checkstory.design.ActiveUser
import dev.szymonchaber.checkstory.design.AdViewModel
import dev.szymonchaber.checkstory.design.theme.CheckstoryTheme
import dev.szymonchaber.checkstory.domain.model.User
import dev.szymonchaber.checkstory.navigation.Navigation
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val appReviewViewModel: AppReviewViewModel by viewModels()

    private val manager by lazy { ReviewManagerFactory.create(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CheckstoryTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colors.background) {
                    val adViewModel = hiltViewModel<AdViewModel>(LocalContext.current as ComponentActivity)
                    val user by adViewModel.currentUserFlow.collectAsState(initial = User.Guest)
                    CompositionLocalProvider(ActiveUser provides user) {
                        Navigation()
                    }
                }
            }
        }
        intent?.getStringExtra(DEEP_LINK_EXTRA)?.let {
            val browserIntent = Intent(Intent.ACTION_VIEW, it.toUri())
            startActivity(browserIntent)
        }
        MobileAds.initialize(this)
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.CREATED) {
                appReviewViewModel.displayReviewEventFlow.collect {
                    val request = manager.requestReviewFlow()
                    request.addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val reviewInfo = task.result
                            val flow = manager.launchReviewFlow(this@MainActivity, reviewInfo)
                            flow.addOnCompleteListener { }
                        } else {
                            // TODO There was some problem, log or handle the error code.
                            val exception = task.exception
                        }
                    }
                }
            }
        }
    }

    companion object {

        private const val DEEP_LINK_EXTRA = "deepLink"
    }
}
