package dev.szymonchaber.checkstory.checklist.catalog

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicInteger

const val NEW_TASK_ID = -50

val indexGenerator = AtomicInteger(0)

val tasks = List(10) {
    val index = indexGenerator.getAndIncrement()

    Task(index, "Task ${index + 1}", List(2) {
        val localIndex = indexGenerator.getAndIncrement()
        Task(localIndex, "Task ${localIndex + 1}", listOf())
    })
}

val LocalDragDropState = compositionLocalOf<DragDropState> {
    error("You must provide LocalDragDropState")
}

@Composable
fun rememberDragDropState(
    lazyListState: LazyListState = rememberLazyListState(),
    unwrappedTasks: List<Pair<Task, Int>>,
): DragDropState {
    return remember {
        DragDropState(
            tasksWithNestedLevel = unwrappedTasks,
            lazyListState = lazyListState,
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun Experiment() {
    var magicTree by remember {
        mutableStateOf(MagicTree(tasks))
    }
    val items by remember {
        derivedStateOf {
            magicTree.flattenWithNestedLevel()
        }
    }
    val dragDropState = rememberDragDropState(unwrappedTasks = items)

    LaunchedEffect(dragDropState.isDragging) {
        val data = if (!dragDropState.isDragging) {
            dragDropState.dataToDrop
        } else {
            null
        }
        data?.let {
            dragDropState.dataToDrop = null
            dragDropState.currentDropTarget?.invoke(it)
        }
    }

    CompositionLocalProvider(
        LocalDragDropState provides dragDropState,
    ) {
        val scope = rememberCoroutineScope()
        var overscrollJob by remember { mutableStateOf<Job?>(null) }
        Box(
            modifier = Modifier
                .fillMaxSize()
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectDragGesturesAfterLongPress(
                            onDrag = { change, offset ->
                                change.consume()
                                dragDropState.onDrag(offset)

                                if (overscrollJob?.isActive == true)
                                    return@detectDragGesturesAfterLongPress

                                dragDropState
                                    .checkForOverScroll()
                                    .takeIf { it != 0f }
                                    ?.let {
                                        overscrollJob =
                                            scope.launch { dragDropState.lazyListState.scrollBy(it) }
                                    }
                                    ?: run { overscrollJob?.cancel() }
                            },
                            onDragStart = { offset -> dragDropState.onDragStart(offset) },
                            onDragEnd = { dragDropState.onDragInterrupted() },
                            onDragCancel = { dragDropState.onDragInterrupted() }
                        )
                    },
                contentPadding = PaddingValues(horizontal = 10.dp),
                state = dragDropState.lazyListState
            ) {
                item {
                    TaskDropTarget {
                        magicTree = magicTree.withTaskMovedToTop(it)
                    }
                }
                items(items = items, key = { it.first.id }) { (task, nestedLevel) ->
                    val startPadding by animateDpAsState(
                        24.dp * nestedLevel
                    )
                    TaskCard(
                        modifier = Modifier
                            .animateItemPlacement()
                            .padding(start = startPadding),
                        task = task,
                        onSiblingTaskDroppedOnto = { newSiblingId ->
                            magicTree = magicTree.withTaskMovedBelow(newSiblingId, below = task)
                        },
                        onChildTaskDroppedUnder = { childTaskId ->
                            magicTree = magicTree.withChildMovedUnderTask(childTaskId, targetTask = task)
                        }
                    )
                }
                item {
                    TaskDropTarget {
                        magicTree = magicTree.withTaskMovedToBottom(it)
                    }
                }
            }
            DropTargetIndicatorLine()
            Draggable(
                dataToDrop = NEW_TASK_ID,
                modifier = Modifier.align(Alignment.BottomStart)
            ) {
                Box(
                    modifier = Modifier
                        .padding(16.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.Cyan)
                        .height(50.dp)
                        .padding(8.dp)
                        .then(it)
                ) {
                    Text(modifier = Modifier.align(Alignment.Center), text = "New task")
                }
            }
            if (dragDropState.isDragging) {
                var targetSize by remember {
                    mutableStateOf(IntSize.Zero)
                }
                Box(
                    modifier = Modifier
                        .graphicsLayer {
                            val offset = (dragDropState.dragPosition + dragDropState.dragOffset)
                            alpha = if (targetSize == IntSize.Zero) 0f else .9f
                            translationX = offset.x//.minus(12.dp.toPx())
//                            translationY = offset.y//.minus(targetSize.height * 2 + 0.dp.toPx())
                            translationY = offset.y
//                                dragDropState.dragPosition.y + (dragDropListStateMine.elementDisplacement ?: 0f)
                        }
                        .onGloballyPositioned {
                            targetSize = it.size
                        }
                ) {
                    dragDropState.draggableComposable?.invoke(Modifier)
                }
            }
        }
    }
}

@Composable
private fun TaskDropTarget(onTaskDropped: (Int) -> Unit) {
    DropTarget(
        modifier = Modifier
            .fillMaxWidth()
            .height(96.dp),
        onDataDropped = onTaskDropped,
        placeTargetLineOnTop = true
    )
}
