package ir.ehsannarmani.compose_charts.models

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.tween
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import ir.ehsannarmani.compose_charts.extensions.format
import ir.ehsannarmani.compose_charts.extensions.line_chart.LabelConverter
import kotlin.reflect.KClass

abstract class Line<T> {
    abstract val label: String?
    abstract val data: List<T>
    abstract val color: Brush
    abstract val firstGradientFillColor: Color
    abstract val secondGradientFillColor: Color
    abstract val drawStyle: DrawStyle
    abstract val strokeAnimationSpec: AnimationSpec<Float>
    abstract val gradientAnimationSpec: AnimationSpec<Float>
    abstract val gradientAnimationDelay: Long
    abstract val dotProperties: DotProperties?
    abstract val popupProperties: PopupProperties?
    abstract val curvedEdges: Boolean?
    abstract val strokeProgress: Animatable<Float, AnimationVector1D>
    abstract val gradientProgress: Animatable<Float, AnimationVector1D>
    abstract var xValues: List<Double>
    abstract val yValues: List<Double>
    abstract val xRange: Range
    abstract val yRange: Range
    val doublePairs: List<Pair<Double, Double>>
        get() = xValues.zip(yValues)
}

data class Range(val start: Double, val end: Double)

class Scatter<T : Any>(
    override val data: List<Pair<T, Double>>,
    override val color: Brush,
    override val label: String? = null,
    override val firstGradientFillColor: Color = Color.Unspecified,
    override val secondGradientFillColor: Color = Color.Unspecified,
    override val drawStyle: DrawStyle = DrawStyle.Stroke(2.dp),
    override val strokeAnimationSpec: AnimationSpec<Float> = tween(2000),
    override val gradientAnimationSpec: AnimationSpec<Float> = tween(2000),
    override val gradientAnimationDelay: Long = 1000,
    override val dotProperties: DotProperties? = null,
    override val popupProperties: PopupProperties? = null,
    override val curvedEdges: Boolean? = null,
    override val strokeProgress: Animatable<Float, AnimationVector1D> = Animatable(0f),
    override val gradientProgress: Animatable<Float, AnimationVector1D> = Animatable(0f),
) : Line<Pair<T, Double>>() {
    override var xValues = emptyList<Double>()
    override val yValues = data.map { it.second }
    override val xRange
        get() = Range(xValues.min(), xValues.max())
    override val yRange = Range(yValues.min(), yValues.max())
}

class Values(
    override val data: List<Double>,
    override val color: Brush,
    override val label: String? = null,
    override val firstGradientFillColor: Color = Color.Unspecified,
    override val secondGradientFillColor: Color = Color.Unspecified,
    override val drawStyle: DrawStyle = DrawStyle.Stroke(2.dp),
    override val strokeAnimationSpec: AnimationSpec<Float> = tween(2000),
    override val gradientAnimationSpec: AnimationSpec<Float> = tween(2000),
    override val gradientAnimationDelay: Long = 1000,
    override val dotProperties: DotProperties? = null,
    override val popupProperties: PopupProperties? = null,
    override val curvedEdges: Boolean? = null,
    override val strokeProgress: Animatable<Float, AnimationVector1D> = Animatable(0f),
    override val gradientProgress: Animatable<Float, AnimationVector1D> = Animatable(0f),
) : Line<Double>() {
    override var xValues = (0..<data.size).map { it.toDouble()/(data.size - 1) }
    override val yValues = data
    override val xRange = Range(0.0, 1.0)
    override val yRange = Range(yValues.min(), yValues.max())
}