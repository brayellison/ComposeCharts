package ir.ehsannarmani.compose_charts.utils

import androidx.annotation.FloatRange
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.DrawScope.Companion.DefaultBlendMode
import androidx.compose.ui.graphics.drawscope.DrawStyle
import androidx.compose.ui.graphics.drawscope.Fill

class Space(
    val bounds: Bounds,
    val xMin: Double,
    xMax: Double,
    yMin: Double,
    val yMax: Double
) {
    constructor(size: Size, xMin: Double, xMax: Double, yMin: Double, yMax: Double) : this(
        Bounds(size), xMin, xMax, yMin, yMax
    )

    val xRange = xMax - xMin
    val yRange = yMin - yMax

    fun pointToOffset(point: Pair<Double, Double>) = pointToOffset(point.first, point.second)
    fun pointToOffset(x: Double, y: Double): Offset {
        val percentX = (x - xMin) / xRange
        val percentY = (y - yMax) / yRange
        return Offset(
            (percentX * bounds.width + bounds.left).toFloat(),
            (percentY * bounds.height + bounds.top).toFloat()
        )
    }

    fun pointsToOffsets(points: List<Pair<Double, Double>>) = points.map { pointToOffset(it) }
    fun pointsToPath(points: List<Pair<Double, Double>>, rounded: Boolean = false): Path {
        val path = Path()
        val offsets = pointsToOffsets(points)
        val first = offsets.firstOrNull() ?: return path
        if (rounded) {
            path.cubicFromOffsets(offsets)
            return path
        }
        path.moveTo(first)
        offsets.drop(1).forEach { path.lineTo(it) }
        return path
    }

    fun DrawScope.drawPointsOnPath(
        points: List<Pair<Double, Double>>,
        color: Color,
        @FloatRange(from = 0.0, to = 1.0) alpha: Float = 1.0f,
        style: DrawStyle = Fill,
        colorFilter: ColorFilter? = null,
        blendMode: BlendMode = DefaultBlendMode,
    ) {
        val path = pointsToPath(points)
        drawPath(
            path = path,
            color = color,
            alpha = alpha,
            style = style,
            colorFilter = colorFilter,
            blendMode = blendMode
        )
    }

    fun translateOut(offset: Offset) = offset + Offset(bounds.left, bounds.top)
    fun translateIn(offset: Offset) = offset - Offset(bounds.left, bounds.top)

    fun offsetToPoint(offset: Offset, isInner: Boolean = false): Pair<Double, Double> {
        val translated = if (isInner) translateOut(offset) else offset
        val percentX = (translated.x - bounds.right) / bounds.width
        val percentY = (translated.y - bounds.top) / bounds.height
        return xMin + percentX * xRange to yMax + percentY * yRange
    }


}

fun Path.moveTo(offset: Offset) {
    this.moveTo(offset.x, offset.y)
}

fun Path.lineTo(offset: Offset) {
    this.lineTo(offset.x, offset.y)
}

fun Path.cubicTo(lastOffset: Offset, nextOffset: Offset) {
    val centerX = (lastOffset.x + nextOffset.x) / 2
    this.cubicTo(centerX, lastOffset.y, centerX, nextOffset.y, nextOffset.x, nextOffset.y)
}

//const val factor = 2f

fun Path.cubicFromOffsets(offsets: List<Offset>) {
    if (offsets.isEmpty()) return
    if (offsets.size == 1) {
        this.moveTo(offsets.first())
        return
    }
    offsets.zipWithNext().forEachIndexed { index, (current, next) ->
        if (index == 0) moveTo(current)
        cubicTo(current, next)
//        val change = next - current
//        val after = offsets.getOrNull(index + 2)
//        val (dx1, dy1) =
//            if (lastDifference == null) {
//                this.moveTo(current)
//                change.x / factor to 0f
//            }
//            else lastDifference.x to lastDifference.y
//        val (dx3, dy3) =
//            if (after == null) change.x / factor to 0f
//            else {
//                val afterNextChange = after - next
//                val dx3 = min((afterNextChange.x)/factor, change.x / factor)
//                val afterCurrentChange = (after - current).let { it/it.x*dx3 }
//                dx3 to afterCurrentChange.y.let {
//                    if (lastDifference?.y == 0f)
//                        return@let change.y
//                    val otherYChange = afterNextChange.y
//                    val minAbs = min(it.absoluteValue, otherYChange.absoluteValue)
//                    if (it.sign == otherYChange.sign) {
//                        minAbs*it.sign
//                    } else {
//                        0f
//                    }
//                }
//            }
//        lastDifference = Offset(dx3, dy3)
//        this.relativeCubicTo(dx1, dy1, change.x - dx3, change.y - dy3, change.x, change.y)
    }
}