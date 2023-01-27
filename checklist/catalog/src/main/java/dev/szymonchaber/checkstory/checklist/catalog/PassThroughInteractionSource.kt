package dev.szymonchaber.checkstory.checklist.catalog

import androidx.compose.foundation.interaction.Interaction
import androidx.compose.foundation.interaction.MutableInteractionSource
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow

class PassThroughInteractionSource(private val interactionSource: MutableInteractionSource) : MutableInteractionSource {

    override val interactions = MutableSharedFlow<Interaction>(
        extraBufferCapacity = 16,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )

    override suspend fun emit(interaction: Interaction) {
        interactions.emit(interaction)
        interactionSource.emit(interaction)
    }

    override fun tryEmit(interaction: Interaction): Boolean {
        interactions.tryEmit(interaction)
        return interactionSource.tryEmit(interaction)
    }
}
