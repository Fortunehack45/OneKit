package com.one.utility.core.designsystem

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput

import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback

enum class ButtonPressState { Pressed, Idle }

/**
 * Adds an ultra-smooth, high-precision tactile press scale feedback with spring damping.
 * Gives cards and buttons a natural, physical, tactile feel at 60/120fps with haptic click.
 */
fun Modifier.pressFeedback(
    pressedScale: Float = 0.965f,
    onClick: (() -> Unit)? = null
): Modifier = composed {
    var buttonState by remember { mutableStateOf(ButtonPressState.Idle) }
    val haptic = LocalHapticFeedback.current

    val scale by animateFloatAsState(
        targetValue = if (buttonState == ButtonPressState.Pressed) pressedScale else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "pressFeedbackScale"
    )

    this
        .graphicsLayer {
            scaleX = scale
            scaleY = scale
        }
        .pointerInput(buttonState) {
            awaitPointerEventScope {
                buttonState = if (buttonState == ButtonPressState.Pressed) {
                    waitForUpOrCancellation()
                    ButtonPressState.Idle
                } else {
                    awaitFirstDown(false)
                    ButtonPressState.Pressed
                }
            }
        }
        .then(
            if (onClick != null) {
                Modifier.clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = {
                        try {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        } catch (_: Exception) {}
                        onClick()
                    }
                )
            } else Modifier
        )
}

/**
 * Standard professional accordion expand/collapse specifications for lists & sections
 */
object AccordionTransitions {
    val expand = expandVertically(
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMediumLow
        )
    ) + fadeIn(animationSpec = tween(durationMillis = 200))

    val collapse = shrinkVertically(
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMediumLow
        )
    ) + fadeOut(animationSpec = tween(durationMillis = 160))
}

/**
 * Standard professional screen transition specifications
 */
object ScreenTransitions {
    val enterTransition = slideInHorizontally(
        initialOffsetX = { (it * 0.12f).toInt() },
        animationSpec = tween(durationMillis = 280, easing = FastOutSlowInEasing)
    ) + fadeIn(animationSpec = tween(durationMillis = 220))

    val exitTransition = slideOutHorizontally(
        targetOffsetX = { -(it * 0.12f).toInt() },
        animationSpec = tween(durationMillis = 280, easing = FastOutSlowInEasing)
    ) + fadeOut(animationSpec = tween(durationMillis = 220))

    val popEnterTransition = slideInHorizontally(
        initialOffsetX = { -(it * 0.12f).toInt() },
        animationSpec = tween(durationMillis = 280, easing = FastOutSlowInEasing)
    ) + fadeIn(animationSpec = tween(durationMillis = 220))

    val popExitTransition = slideOutHorizontally(
        targetOffsetX = { (it * 0.12f).toInt() },
        animationSpec = tween(durationMillis = 280, easing = FastOutSlowInEasing)
    ) + fadeOut(animationSpec = tween(durationMillis = 220))
}

