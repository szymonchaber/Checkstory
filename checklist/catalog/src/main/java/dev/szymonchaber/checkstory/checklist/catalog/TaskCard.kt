package dev.szymonchaber.checkstory.checklist.catalog

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import timber.log.Timber

@Composable
fun TaskCard(
    modifier: Modifier,
    task: Task,
    onSiblingTaskDroppedOnto: (Int) -> Unit,
    onChildTaskDroppedUnder: (Int) -> Unit
) {
    Card(
        elevation = 10.dp,
        backgroundColor = Color.White,
        modifier = modifier.padding(8.dp)
    ) {
        Box(Modifier.height(IntrinsicSize.Min)) {
            Draggable(modifier = Modifier, dataToDrop = task.id, onDragStart = {
                Timber.d("Starting the drag with data: $it for actual task: $task ")
            }) { dragHandleModifier ->
                Row(
                    modifier = Modifier
                        .background(Color.White)
                        .padding(10.dp)
                        .fillMaxWidth()
                ) {
                    Icon(
                        modifier = Modifier
                            .align(Alignment.CenterVertically)
                            .then(dragHandleModifier),
                        painter = painterResource(id = R.drawable.drag_indicator),
                        contentDescription = null
                    )
                    Spacer(modifier = Modifier.width(20.dp))
                    Text(
                        text = task.name,
                        fontSize = 22.sp,
                        color = Color.DarkGray
                    )
                }
            }
            Receptacles(
                task = task,
                onSiblingTaskDropped = { siblingTask ->
                    onSiblingTaskDroppedOnto(siblingTask)
                },
                onChildTaskDropped = { childTask ->
                    onChildTaskDroppedUnder(childTask)
                }
            )
        }
    }
}

@Composable
private fun Receptacles(
    task: Task,
    onSiblingTaskDropped: (Int) -> Unit,
    onChildTaskDropped: (Int) -> Unit
) {
    Row(Modifier.fillMaxSize()) {
        DropTarget(
            modifier = Modifier
                .fillMaxHeight()
                .width(24.dp), // TODO decide
            key = task,
            onDataDropped = { siblingTask ->
                onSiblingTaskDropped(siblingTask)
            }
        )
        DropTarget(
            modifier = Modifier
                .fillMaxHeight()
                .weight(1f),
            key = task,
            onDataDropped = { childTask ->
                onChildTaskDropped(childTask)
            }
        )
    }
}
