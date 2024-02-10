@file:OptIn(ExperimentalTextApi::class)

package com.vero.glovolikeanimation

import androidx.compose.foundation.Canvas
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
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
import kotlin.math.cos
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
    mainCircleRadius: Dp = 130.dp,
    innerCircleRadius: Dp = 60.dp,
    textStyle: TextStyle = MaterialTheme.typography.bodyMedium,
    onGoalClick: (GlovoItem) -> Unit,
) {

    var circleCenter by remember { mutableStateOf(Offset.Zero) }

//  Object that will help us to measure and customize text
    val textMeasurer = rememberTextMeasurer()

    Canvas(modifier = modifier) {

        //Save center of the canvas
        circleCenter = Offset(center.x, center.y)

        //Count the distance for items around the circle
        val distance = 360f / items.size

        drawCircleInfo(
            item = mainItem,
            angleInRad = 0f,
            mainCircleRadius = 0.dp,
            innerCircleRadius = innerCircleRadius,
            iconScale = iconScale,
            textStyle = textStyle,
            textMeasurer = textMeasurer,
            circleCenter = circleCenter
        )

//      Draw secondary items
        items.forEachIndexed { i, item ->
            //            Firstly count the angle on which we should position secondary item in degrees
//            I'm not sure about -90 but this help to position degrees in the right position (you can play around with that)
            val angleInDegrees = (i * distance - 90)

//          Convert angle to radians
            val angleInRad = angleInDegrees * (PI / 180).toFloat()
            drawCircleInfo(
                item = item,
                angleInRad = angleInRad,
                mainCircleRadius = mainCircleRadius,
                innerCircleRadius = innerCircleRadius,
                iconScale = iconScale,
                textStyle = textStyle,
                textMeasurer = textMeasurer,
                circleCenter = circleCenter
            )
        }
    }
}

fun DrawScope.drawCircleInfo(
    item: GlovoItem,
    angleInRad: Float,
    mainCircleRadius: Dp,
    innerCircleRadius: Dp,
    iconScale: Float,
    textStyle: TextStyle,
    textMeasurer: TextMeasurer,
    circleCenter: Offset
) {
//  In essence, this function helps find the coordinates of a point on a circle based
//  on its radius (mainCircleRadius), center (circleCenter), and the angle (angleInRad)
//  at which you want to find the point.
    val currentOffset = Offset(
        x = mainCircleRadius.toPx() * cos(angleInRad) + circleCenter.x,
        y = mainCircleRadius.toPx() * sin(angleInRad) + circleCenter.y
    )
//  Draw secondary items with counted offset
    drawCircle(
        color = Color.White,
        radius = innerCircleRadius.toPx(),
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
        scale(scale = iconScale, pivot = pathBounds.topLeft) {
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
            fontSize = textStyle.fontSize
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