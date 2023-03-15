package dev.szymonchaber.checkstory.checklist.catalog

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateOffsetAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

val tasks = listOf(
    Task(1, "Pizza", 20.0, Color.Blue),
    Task(2, "French toast", 10.05, Color.Cyan),
    Task(3, "Chocolate cake", 12.99, Color.Magenta),
)
val persons = listOf(
    Person(1, "Jhone", Color.Green),
    Person(2, "Eyle", Color.Yellow),
    Person(3, "Tommy", Color.Gray),
)

@Composable
fun Experiment() {
    LongPressDraggable(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 10.dp)
        ) {
            items(items = tasks) { task ->
                TaskCard(task = task)
            }
        }
        PersonListContainer(persons)
        TargetTodoistLine()
    }
}

@Composable
private fun TargetTodoistLine() {
    val state = LocalDragTargetInfo.current
    val targetState = LocalTargetedItemInfo.current

    val targetOffset = (state.dragPosition + state.dragOffset)
    val offset by animateOffsetAsState(targetValue = targetOffset)
    AnimatedVisibility(visible = targetState.targetedItemSet.isNotEmpty()) {
        Card(
            elevation = 20.dp,
            backgroundColor = Color.White,
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier
                .padding(8.dp)
                .graphicsLayer {
//                scaleX = 1.3f
//                scaleY = 1.3f
                    translationX = offset.x
                    translationY = offset.y
                }
                .size(50.dp)
        ) {

        }
    }
}

@Composable
private fun BoxScope.PersonListContainer(persons: List<Person>) {
    LazyRow(
        modifier = Modifier
            .fillMaxHeight(0.3f)
            .fillMaxWidth()
            .background(Color.LightGray, shape = RoundedCornerShape(topEnd = 10.dp, topStart = 10.dp))
            .padding(vertical = 10.dp)
            .align(Alignment.BottomCenter),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        items(items = persons) { person ->
            PersonCard(person)
        }
    }
}


internal class DragTargetInfo {
    var isDragging: Boolean by mutableStateOf(false)
    var dragPosition by mutableStateOf(Offset.Zero)
    var dragOffset by mutableStateOf(Offset.Zero)
    var draggableComposable by mutableStateOf<(@Composable () -> Unit)?>(null)
    var dataToDrop by mutableStateOf<Any?>(null)
}

internal class TargetedItemInfo {
    var targetedItemSet: Set<Any> by mutableStateOf(setOf())
}

internal val LocalDragTargetInfo = compositionLocalOf { DragTargetInfo() }
internal val LocalTargetedItemInfo = compositionLocalOf { TargetedItemInfo() }
@Composable
fun TaskCard(task: Task) {
    Card(
        elevation = 10.dp, backgroundColor = Color.White,
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier.padding(8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(10.dp)
        ) {
            Draggable(modifier = Modifier, dataToDrop = task) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(task.color)
                        .size(130.dp)
                )
            }
            Spacer(modifier = Modifier.width(20.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = task.name,
                    fontSize = 22.sp,
                    color = Color.DarkGray
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "$${task.price}",
                    fontSize = 18.sp,
                    color = Color.Black,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }
    }
}

@Composable
fun LongPressDraggable(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    val targetState = remember { TargetedItemInfo() }
    val state = remember { DragTargetInfo() }
    CompositionLocalProvider(
        LocalDragTargetInfo provides state,
        LocalTargetedItemInfo provides targetState
    ) {
        Box(
            modifier = modifier
                .fillMaxSize()
        ) {
            content()
            if (state.isDragging) {
                var targetSize by remember {
                    mutableStateOf(IntSize.Zero)
                }
                Box(modifier = Modifier
                    .graphicsLayer {
                        val offset = (state.dragPosition + state.dragOffset)
                        scaleX = 1.3f
                        scaleY = 1.3f
                        alpha = if (targetSize == IntSize.Zero) 0f else .9f
                        translationX = offset.x.minus(targetSize.width / 2)
                        translationY = offset.y.minus(targetSize.height / 2)
                    }
                    .onGloballyPositioned {
                        targetSize = it.size
                    }
                ) {
                    state.draggableComposable?.invoke()
                }
            }
        }
    }
}

data class Person(val id: Int, val name: String, val color: Color)

@Composable
fun PersonCard(person: Person) {
    val foodItems = remember {
        mutableStateMapOf<Int, Task>()
    }
    DropTarget<Task>(
        modifier = Modifier
            .padding(6.dp)
            .width(width = 120.dp)
            .fillMaxHeight(0.8f),
        person
    ) { isInBound, foodItem ->
        val bgColor = if (isInBound) Color.Red else Color.White

        foodItem?.let {
            if (isInBound)
                foodItems[foodItem.id] = foodItem
        }

        Column(
            modifier = Modifier.background(bgColor, RoundedCornerShape(16.dp)),
        ) {
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(person.color)
                    .size(70.dp),
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = person.name,
                fontSize = 18.sp,
                color = Color.Black,
                fontWeight = FontWeight.Bold
            )

            if (foodItems.isNotEmpty()) {
                Text(
                    text = "$${foodItems.values.sumOf { it.price }}",
                    fontSize = 16.sp,
                    color = Color.Black,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    text = "${foodItems.size} Items",
                    fontSize = 14.sp,
                    color = Color.Black
                )
            }
        }
    }
}

data class Task(val id: Int, val name: String, val price: Double, val color: Color) {

}
