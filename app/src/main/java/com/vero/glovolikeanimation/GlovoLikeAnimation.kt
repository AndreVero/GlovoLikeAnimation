@file:OptIn(ExperimentalTextApi::class)

package com.vero.glovolikeanimation

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

data class GlovoItem(
    val title: String,
    val path: Path
)

@Composable
fun GlovoLikeAnimation(
    modifier: Modifier = Modifier,
    mainItem: GlovoItem,
//  Scale factor for icon
    iconScale: Float = 3f,
    items: List<GlovoItem> = emptyList(),
    mainCircleRadius: Dp = 140.dp,
    innerCircleRadius: Dp = 60.dp,
    textStyle: TextStyle = MaterialTheme.typography.bodyMedium,
    onGoalClick: (GlovoItem) -> Unit,
) {

    val glovoUiItems = remember { mutableStateMapOf<Offset, GlovoItem>() }
    val animateFloat = remember { Animatable(0f) }
    var circleCenter by remember { mutableStateOf(Offset.Zero) }

//  Current drag angle
    var angle by remember { mutableStateOf(0f) }

//  Start angle of a new drag
    var dragStartedAngle by remember { mutableStateOf(0f) }

//  Variable in which we will need to calculate difference between old drag position and new
    var oldAngle by remember { mutableStateOf(angle) }

    LaunchedEffect(key1 = items) {
        animateFloat.animateTo(
            targetValue = 1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessVeryLow
            )
        )
    }

//  Object that will help us to measure and customize text
    val textMeasurer = rememberTextMeasurer()

    Canvas(modifier = modifier
//       Check that click offset was inside a circle
        .pointerInput(true) {
            detectTapGestures { clickOffset ->
                glovoUiItems.forEach { item ->
                    val rect = Rect(item.key, innerCircleRadius.toPx())
                    if (rect.contains(clickOffset)) {
                        onGoalClick(item.value)
                    }
                }
            }
        }
        .pointerInput(true) {
            detectDragGestures(
                onDragStart = { offset ->
                    // Calculate the angle of the drag started point relative to circleCenter
                    dragStartedAngle = -atan2(
                        circleCenter.x - offset.x,
                        circleCenter.y - offset.y,
                    ) * (180f / PI.toFloat())
                    // Adjust the angle to fit within the range of 0 to 360 degrees
                    dragStartedAngle = (dragStartedAngle + 180f).mod(360f)
                },
                onDragEnd = {
                    // Save the current angle as oldAngle when the drag ends
                    oldAngle = angle
                }
            ) { change, _ ->
//              Calculate the angle of the current drag position relative to circleCenter
                var touchAngle = -atan2(
                    circleCenter.x - change.position.x,
                    circleCenter.y - change.position.y,
                ) * (180f / PI.toFloat())
                touchAngle = (touchAngle + 180f).mod(360f)

//              Calculate the change in angle from the start of the drag to the current position
                val changeAngle = touchAngle - dragStartedAngle

//              Update the angle based on the change in angle from the start of the drag
                angle = (oldAngle + (changeAngle).roundToInt())
            }
        }
    ) {

//      Clear circles offset before recalculation
        glovoUiItems.clear()

//      Save center of the canvas
        circleCenter = Offset(center.x, center.y)

        //Count the distance for items around the circle
        val distance = 360f / items.size
        val mainCircleOffset = Offset(x = circleCenter.x, y = circleCenter.y)
        glovoUiItems[mainCircleOffset] = mainItem

//      Draw main circle in the center
        drawCircleInfo(
            item = mainItem,
            innerCircleRadius = innerCircleRadius,
            iconScale = iconScale,
            textStyle = textStyle,
            textMeasurer = textMeasurer,
            animationValue = animateFloat.value,
            currentOffset = mainCircleOffset
        )

//      Draw secondary items
        items.forEachIndexed { i, item ->

//          Firstly count the angle on which we
//          should position secondary item in degrees
//          -90 will help to position degrees in
//          the right appropriately (you can play around with that)
            val angleInDegrees = (i * distance + angle - 90)

//          Convert angle to radians
            val angleInRad = angleInDegrees * (PI / 180).toFloat()
//          In essence, this function helps
//          find the coordinates of a point on a circle based
//          on its radius (mainCircleRadius),
//          center (circleCenter), and the angle (angleInRad)
//          at which you want to find the point.
            val currentOffset = Offset(
                x = mainCircleRadius.toPx() * cos(angleInRad) + circleCenter.x,
                y = mainCircleRadius.toPx() * sin(angleInRad) + circleCenter.y
            )

//          Add circle offset after recalculation
            glovoUiItems[currentOffset] = item

            drawCircleInfo(
                item = item,
                innerCircleRadius = innerCircleRadius,
                iconScale = iconScale,
                textStyle = textStyle,
                textMeasurer = textMeasurer,
                animationValue = animateFloat.value,
                currentOffset = currentOffset
            )
        }
    }
}

fun DrawScope.drawCircleInfo(
    item: GlovoItem,
    innerCircleRadius: Dp,
    iconScale: Float,
    textStyle: TextStyle,
    textMeasurer: TextMeasurer,
    animationValue: Float,
    currentOffset: Offset
) {

//  Draw secondary items with counted offset
    drawCircle(
        color = Color.White,
        radius = innerCircleRadius.toPx() * animationValue,
        center = currentOffset
    )

//  Get the path bounds to translate path exactly to the center of the circle
    val pathBounds = item.path.getBounds()

//  Move our path to the new position
    translate(
        left = currentOffset.x - (pathBounds.right * iconScale) / 2,
        top = currentOffset.y - 15.dp.toPx() - pathBounds.bottom
    ) {
        // Increase path in 3 times
        scale(scale = iconScale * animationValue, pivot = pathBounds.topLeft) {
            drawPath(
                path = item.path,
                color = Color.Black
            )
        }
    }

//  Calculate all needed info for text drawing
    val measurementResult = textMeasurer.measure(
        item.title,
        constraints = Constraints(
//          Set constraints to prevent overflow
            maxWidth = (innerCircleRadius.toPx() * 2 - 16.dp.toPx()).toInt()
        ),

//      Set style for the text
        style = textStyle.copy(
            textAlign = TextAlign.Center,
            fontSize = textStyle.fontSize * animationValue
        )
    )

    drawText(
        textLayoutResult = measurementResult,
//      For text we need to set top left corner from which drawing will be started
//      in this case I set text in the middle
        topLeft = Offset(
            x = currentOffset.x - measurementResult.size.width / 2,
            y = currentOffset.y + pathBounds.height
        )
    )
}