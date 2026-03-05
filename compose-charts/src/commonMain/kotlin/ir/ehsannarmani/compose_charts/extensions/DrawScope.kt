package ir.ehsannarmani.compose_charts.extensions

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.inset
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.drawText
import androidx.compose.ui.unit.toSize
import ir.ehsannarmani.compose_charts.models.DotProperties
import ir.ehsannarmani.compose_charts.models.GridProperties
import ir.ehsannarmani.compose_charts.models.HorizontalIndicatorProperties
import ir.ehsannarmani.compose_charts.models.IndicatorPosition
import ir.ehsannarmani.compose_charts.models.LabelProperties
import ir.ehsannarmani.compose_charts.models.PopupProperties
import ir.ehsannarmani.compose_charts.utils.InsetPad
import ir.ehsannarmani.compose_charts.utils.bounds
import ir.ehsannarmani.compose_charts.utils.calculateOffset
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

fun DrawScope.inset(insetPad: InsetPad, block: DrawScope.() -> Unit) {
    val mostLeft = size.width / 2f
    val mostTop = size.height / 2f
    inset(
        left = insetPad.left.coerceAtMost(mostLeft),
        right = insetPad.right.coerceAtMost(size.width - mostLeft),
        top = insetPad.top.coerceAtMost(mostTop),
        bottom = insetPad.bottom.coerceAtMost(size.height - mostTop),
    ) {
        block()
    }
}

internal sealed class TickDirection {
    data object Horizontal : TickDirection()
    data object Vertical : TickDirection()
}

internal fun DrawScope.drawTicks(
    ticks: List<Float>,
    tickDirection: TickDirection,
    gridEnabled: Boolean,
    axisProperties: GridProperties.AxisProperties,
) {
    if (gridEnabled && axisProperties.enabled) {
        val getOffsets = { tick: Float ->
            if (tickDirection == TickDirection.Horizontal) {
                Offset(0f, tick) to Offset(size.width, tick)
            } else {
                Offset(tick, 0f) to Offset(tick, size.height)
            }
        }
        ticks
            .ifEmpty {
                (0 until axisProperties.lineCount)
                    .map { it.toFloat() * size.width / (axisProperties.lineCount - 1) }
            }
            .forEach { tick ->
                val (start, end) = getOffsets(tick)
                drawLine(
                    brush = axisProperties.color,
                    start = start,
                    end = end,
                    strokeWidth = axisProperties.thickness.toPx(),
                    pathEffect = axisProperties.style.pathEffect,
                )
            }
    }
}

data class DotInfo(
    val animator: Animatable<Float, AnimationVector1D>,
    val dataIndex: Int,
    val value: Float,
    val offset: Offset
)

internal fun DrawScope.drawDots(
    dataPoints: List<DotInfo>,
    properties: DotProperties,
    linePath: Path,
    pathMeasure: PathMeasure,
    scope: CoroutineScope,
) {

    val pathEffect = properties.strokeStyle.pathEffect

    pathMeasure.setPath(linePath, false)
    val lastPosition = pathMeasure.getPosition(pathMeasure.length)
    dataPoints.forEachIndexed { valueIndex, value ->
        if (
            properties.confirmDraw(
                DotProperties.Dot(
                    value.dataIndex,
                    valueIndex,
                    value.value.toDouble()
                )
            ) //&&
//            valueIndex in startIndex..endIndex
        ) {
            if (lastPosition != Offset.Unspecified && lastPosition.x >= value.offset.x - 20 || !properties.animationEnabled || dataPoints.count() == 1) {
                if (!value.animator.isRunning && properties.animationEnabled && value.animator.value != 1f) {
                    scope.launch {
                        value.animator.animateTo(1f, animationSpec = properties.animationSpec)
                    }
                }

                val radius: Float
                val strokeRadius: Float
                if (properties.animationEnabled) {
                    radius =
                        (properties.radius.toPx() + properties.strokeWidth.toPx() / 2) * value.animator.value
                    strokeRadius = properties.radius.toPx() * value.animator.value
                } else {
                    radius = properties.radius.toPx() + properties.strokeWidth.toPx() / 2
                    strokeRadius = properties.radius.toPx()
                }
                drawCircle(
                    brush = properties.strokeColor,
                    radius = radius,
                    center = value.offset,
                    style = Stroke(width = properties.strokeWidth.toPx(), pathEffect = pathEffect),
                )
                drawCircle(
                    brush = properties.color,
                    radius = strokeRadius,
                    center = value.offset,
                )
            }
        }
    }
}

internal data class Popup(
    val properties: PopupProperties,
    val position: Offset,
    val value: Double,
    val dataIndex: Int,
    val valueIndex: Int
)

internal fun DrawScope.drawPopup(
    popup: Popup,
    nextPopup: Popup?,
    textMeasurer: TextMeasurer,
    scope: CoroutineScope,
    progress: Float,
    offsetAnimator: Pair<Animatable<Float, AnimationVector1D>, Animatable<Float, AnimationVector1D>>? = null
) {
    val popupProperties = popup.properties
    val popupData = PopupProperties.Popup(
        dataIndex = popup.dataIndex,
        valueIndex = popup.valueIndex,
        value = popup.value
    )
    if (!popupProperties.confirmDraw(popupData)) return

    val offset = popup.position
    val measureResult = textMeasurer.measure(
        popupProperties.contentBuilder(popupData),
        style = popupProperties.textStyle.copy(
            color = popupProperties.textStyle.color.copy(
                alpha = 1f * progress
            )
        )
    )
    var rectSize = measureResult.size.toSize()
    rectSize = rectSize.copy(
        width = (rectSize.width + (popupProperties.contentHorizontalPadding.toPx() * 2)),
        height = (rectSize.height + (popupProperties.contentVerticalPadding.toPx() * 2))
    )

    val conflictDetected =
        ((nextPopup != null) && offset.y in nextPopup.position.y - rectSize.height..nextPopup.position.y + rectSize.height) ||
                (offset.x + rectSize.width) > size.width


    val rectOffset = if (conflictDetected) {
        offset.copy(x = offset.x - rectSize.width)
    } else {
        offset
    }
    offsetAnimator?.also { (x, y) ->
        if (x.value == 0f || y.value == 0f || popupProperties.mode is PopupProperties.Mode.PointMode) {
            scope.launch {
                x.snapTo(rectOffset.x)
                y.snapTo(rectOffset.y)
            }
        } else {
            scope.launch {
                x.animateTo(rectOffset.x)
            }
            scope.launch {
                y.animateTo(rectOffset.y)
            }
        }

    }
    if (offsetAnimator != null) {
        var animatedOffset = if (popup.properties.mode is PopupProperties.Mode.PointMode) {
            rectOffset
        } else {
            Offset(
                x = offsetAnimator.first.value,
                y = offsetAnimator.second.value
            )
        }
        var rect = Rect(
            offset = animatedOffset,
            size = rectSize
        )
        if (rect.top < 0) rect = rect.copy(top = 0f, bottom = rect.height)
        if (rect.bottom > size.height)
            rect = rect.copy(top = size.height - rect.height, bottom = size.height)
        if (rect.left < 0) rect = rect.copy(left = 0f, right = rect.width)
        if (rect.right > size.width)
            rect = rect.copy(left = size.width - rect.width, right = size.width)

        animatedOffset = Offset(
            rect.left + popupProperties.contentHorizontalPadding.toPx(),
            rect.top + popupProperties.contentVerticalPadding.toPx()
        )
        drawPath(
            path = Path().apply {
                addRoundRect(popupRect(
                    originalOffset = popup.position,
                    rect = rect,
                    left = !conflictDetected,
                    cornerRadius = popupProperties.cornerRadius.toPx()
                ))
            },
            color = popupProperties.containerColor,
            alpha = 1f * progress
        )
        drawText(
            textLayoutResult = measureResult,
            topLeft = animatedOffset
        )
    }
}

fun popupRect(
    originalOffset: Offset,
    rect: Rect,
    left: Boolean,
    cornerRadius: Float
): RoundRect {
    val roundCorner = CornerRadius(cornerRadius)
    val sharpCorner = CornerRadius(0f)
    val bottom = originalOffset.y > rect.top + rect.height/2f
    return RoundRect(
        rect = rect,
        topLeft = if (!bottom && left) sharpCorner else roundCorner,
        topRight = if (!bottom && !left) sharpCorner else roundCorner,
        bottomRight = if (bottom && !left) sharpCorner else roundCorner,
        bottomLeft = if (bottom && left) sharpCorner else roundCorner,
    )
}

internal fun DrawScope.getTicksAndDrawLabels(
    labelProperties: LabelProperties,
    measureLabel: (String) -> TextLayoutResult,
    insetPad: InsetPad,
    textMeasurer: TextMeasurer,
    xMin: Double,
    xMax: Double,
    labelValues: List<Double>,
): List<Float> {
    return if (labelProperties.enabled && labelProperties.labels.isNotEmpty()) {
        val labels = labelProperties.labels
        val maxLabelHeight = labels.maxOf {
            measureLabel(it).size.height
        }
        val insetBounds = insetPad.toBounds(size)
        val bottomStart = insetBounds.bottom + measureLabel("M").size.width.let {
            if (insetPad.shouldRotate) it * 2f / 3 else it.toFloat()
        }
        val range = xMax - xMin

        labels.zip(labelValues).map { (label, value) ->
            val tick = (insetBounds.width * (value - xMin) / range).toFloat()
            val offset = insetBounds.left + tick
            val textSize = measureLabel(label)
            if (insetPad.shouldRotate) {
                val degrees = labelProperties.rotation.degree
                val bounds = textSize.bounds
                val rotatedBounds = bounds.rotate(-degrees)
                val center = Offset(offset, bottomStart + rotatedBounds.height / 2f)
                rotate(degrees, pivot = center) {
                    drawText(
                        textMeasurer = textMeasurer,
                        text = label,
                        style = labelProperties.textStyle,
                        topLeft = center - Offset(
                            textSize.size.width / 2f,
                            textSize.size.height / 2f
                        )
                    )
                }
            } else {
                drawText(
                    textMeasurer = textMeasurer,
                    text = label,
                    style = labelProperties.textStyle,
                    topLeft = Offset(
                        offset - textSize.size.width / 2f,
                        size.height - maxLabelHeight
                    )
                )
            }
            tick
        }
    } else emptyList()
}

internal fun DrawScope.getTicksAndDrawIndicators(
    indicators: List<Double>,
    indicatorProperties: HorizontalIndicatorProperties,
    minValue: Double,
    maxValue: Double,
    measureIndicator: (String) -> TextLayoutResult,
    insetPad: InsetPad,
    textMeasurer: TextMeasurer
): List<Float> {
    return if (indicators.isNotEmpty() && indicatorProperties.enabled) {
        val sortedIndicators = indicators.sortedBy { -it }.filter { it in minValue..maxValue }
        val maxIndicatorWidth = sortedIndicators.maxOf {
            measureIndicator(indicatorProperties.contentBuilder(it)).size.width
        }
        val drawingHeight = size.height - insetPad.top - insetPad.bottom
        val getTick = { value: Double ->
            drawingHeight - calculateOffset(
                maxValue,
                minValue,
                drawingHeight,
                value.toFloat()
            )
        }
        sortedIndicators.map {
            val tick = getTick(it)
            val offset = tick + insetPad.top
            val text = indicatorProperties.contentBuilder(it)
            val textSize = measureIndicator(text)
            if (indicatorProperties.position == IndicatorPosition.Horizontal.Start) {
                drawText(
                    textMeasurer = textMeasurer,
                    text = text,
                    style = indicatorProperties.textStyle,
                    topLeft = Offset(
                        (maxIndicatorWidth - textSize.size.width).toFloat(),
                        offset.toFloat() - textSize.size.height / 2f
                    )
                )
            } else {
                drawText(
                    textMeasurer = textMeasurer,
                    text = text,
                    style = indicatorProperties.textStyle,
                    topLeft = Offset(
                        (size.width - maxIndicatorWidth),
                        offset.toFloat() - textSize.size.height / 2f
                    )
                )
            }
            tick.toFloat()
        }
    } else emptyList()
}
