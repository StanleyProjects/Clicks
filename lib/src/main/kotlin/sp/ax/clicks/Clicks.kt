package sp.ax.clicks

import androidx.compose.foundation.Indication
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.gestures.PressGestureScope
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.debugInspectorInfo

fun Modifier.clicks(
    enabled: Boolean = true,
    interactionSource: MutableInteractionSource,
    indication: Indication,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
): Modifier {
    return composed(
        inspectorInfo = debugInspectorInfo {
            name = "clicks"
            properties["enabled"] = enabled
            properties["onClick"] = onClick
            properties["onLongClick"] = onLongClick
            properties["indication"] = indication
            properties["interactionSource"] = interactionSource
        },
        factory = {
            val onClickState = rememberUpdatedState(onClick)
            val onLongClickState = rememberUpdatedState(onLongClick)
            val lastPressState = getLastPressState(
                enabled = enabled,
                interactionSource = interactionSource,
            )
            Modifier
                .indication(interactionSource = interactionSource, indication = indication)
                .pointerInput(interactionSource, enabled) {
                    detectTapGestures(
                        onPress = { offset ->
                            if (enabled) {
                                onPress(
                                    offset = offset,
                                    lastPressState = lastPressState,
                                    interactionSource = interactionSource,
                                )
                            }
                        },
                        onTap = {
                            if (enabled) onClickState.value()
                        },
                        onLongPress = {
                            if (enabled) onLongClickState.value()
                        },
                    )
                }
        },
    )
}

fun Modifier.clicks(
    enabled: Boolean = true,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
): Modifier {
    return composed {
        clicks(
            enabled = enabled,
            interactionSource = remember { MutableInteractionSource() },
            indication = LocalIndication.current,
            onClick = onClick,
            onLongClick = onLongClick,
        )
    }
}

@Composable
internal fun getLastPressState(
    enabled: Boolean,
    interactionSource: MutableInteractionSource,
): MutableState<PressInteraction.Press?> {
    val lastPressState = remember { mutableStateOf<PressInteraction.Press?>(null) }
    LaunchedEffect(lastPressState.value, enabled) {
        val lastPress = lastPressState.value
        if (lastPress != null && !enabled) {
            interactionSource.emit(PressInteraction.Cancel(lastPress))
            lastPressState.value = null
        }
    }
    return lastPressState
}

internal suspend fun PressGestureScope.onPress(
    offset: Offset,
    lastPressState: MutableState<PressInteraction.Press?>,
    interactionSource: MutableInteractionSource,
) {
    val press = PressInteraction.Press(offset)
    lastPressState.value = press
    interactionSource.emit(press)
    if (tryAwaitRelease()) {
        interactionSource.emit(PressInteraction.Release(press))
    } else {
        interactionSource.emit(PressInteraction.Cancel(press))
    }
    lastPressState.value = null
}
