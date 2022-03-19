package dev.szymonchaber.checkstory

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Card
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import dagger.hilt.android.AndroidEntryPoint
import dev.szymonchaber.checkstory.checklist.catalog.model.ChecklistCatalogEffect
import dev.szymonchaber.checkstory.checklist.catalog.model.ChecklistCatalogEvent
import dev.szymonchaber.checkstory.checklist.catalog.model.ChecklistCatalogLoadingState
import dev.szymonchaber.checkstory.checklist.catalog.model.ChecklistCatalogState
import dev.szymonchaber.checkstory.checklist.catalog.model.ChecklistCatalogViewModel
import dev.szymonchaber.checkstory.design.theme.CheckstoryTheme
import dev.szymonchaber.checkstory.domain.model.checklist.template.ChecklistTemplate
import dev.szymonchaber.checkstory.navigation.CheckstoryScreens
import dev.szymonchaber.checkstory.navigation.Navigation

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CheckstoryTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colors.background) {
                    Navigation()
                }
            }
        }
    }
}

@Composable
fun MainScreen(viewModel: ChecklistCatalogViewModel, navController: NavHostController) {
    Column(
        modifier = Modifier.padding(top = 16.dp, start = 16.dp, end = 16.dp)
    ) {
        Text(
            text = "Checklists",
            style = MaterialTheme.typography.h5
        )
        val checklistCatalogState by viewModel.state.collectAsState(initial = ChecklistCatalogState.initial)

        val checklistCatalogEffect by viewModel.effect.collectAsState(initial = null)
        LaunchedEffect(checklistCatalogEffect) {
            when (checklistCatalogEffect) {
                is ChecklistCatalogEffect.CreateAndNavigateToChecklist -> {
                    navController.navigate(CheckstoryScreens.DetailsScreen.route)
                }
                null -> Unit
            }
        }
        when (val state = checklistCatalogState.loadingState) {
            ChecklistCatalogLoadingState.Loading -> {
                Text(text = "Loading")
            }
            is ChecklistCatalogLoadingState.Success -> {
                state.checklistTemplates.forEach {
                    ChecklistTemplateView(it, viewModel::onEvent)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun ChecklistTemplateView(
    checklistTemplate: ChecklistTemplate,
    eventListener: (ChecklistCatalogEvent) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp),
        elevation = 4.dp,
        onClick = {
            eventListener(ChecklistCatalogEvent.ChecklistTemplateClicked(checklistTemplate.title)) // TODO change to id
        }
    ) {
        Text(
            modifier = Modifier.padding(16.dp),
            text = checklistTemplate.title,
            style = MaterialTheme.typography.subtitle1
        )
    }
}
