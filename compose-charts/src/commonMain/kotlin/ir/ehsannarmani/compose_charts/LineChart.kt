@file:Suppress("UNCHECKED_CAST")

package ir.ehsannarmani.compose_charts

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.isSpecified
import androidx.compose.ui.graphics.takeOrElse
import androidx.compose.ui.input.pointer.PointerInputScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ir.ehsannarmani.compose_charts.components.LabelHelper
import ir.ehsannarmani.compose_charts.extensions.DotInfo
import ir.ehsannarmani.compose_charts.extensions.Popup
import ir.ehsannarmani.compose_charts.extensions.TickDirection
import ir.ehsannarmani.compose_charts.extensions.drawDots
import ir.ehsannarmani.compose_charts.extensions.drawPopup
import ir.ehsannarmani.compose_charts.extensions.drawTicks
import ir.ehsannarmani.compose_charts.extensions.getTicksAndDrawIndicators
import ir.ehsannarmani.compose_charts.extensions.getTicksAndDrawLabels
import ir.ehsannarmani.compose_charts.extensions.inset
import ir.ehsannarmani.compose_charts.extensions.line_chart.LabelConverter
import ir.ehsannarmani.compose_charts.extensions.line_chart.drawLineGradient
import ir.ehsannarmani.compose_charts.extensions.offsetOnSegment
import ir.ehsannarmani.compose_charts.extensions.segments
import ir.ehsannarmani.compose_charts.extensions.split
import ir.ehsannarmani.compose_charts.extensions.startEnd
import ir.ehsannarmani.compose_charts.extensions.t
import ir.ehsannarmani.compose_charts.models.AnimationMode
import ir.ehsannarmani.compose_charts.models.DotProperties
import ir.ehsannarmani.compose_charts.models.DrawStyle
import ir.ehsannarmani.compose_charts.models.GridProperties
import ir.ehsannarmani.compose_charts.models.HorizontalIndicatorProperties
import ir.ehsannarmani.compose_charts.models.IndicatorPosition
import ir.ehsannarmani.compose_charts.models.LabelHelperProperties
import ir.ehsannarmani.compose_charts.models.LabelProperties
import ir.ehsannarmani.compose_charts.models.Line
import ir.ehsannarmani.compose_charts.models.PopupProperties
import ir.ehsannarmani.compose_charts.models.Scatter
import ir.ehsannarmani.compose_charts.models.Values
import ir.ehsannarmani.compose_charts.models.ZeroLineProperties
import ir.ehsannarmani.compose_charts.utils.InsetPad
import ir.ehsannarmani.compose_charts.utils.Space
import ir.ehsannarmani.compose_charts.utils.bounds
import ir.ehsannarmani.compose_charts.utils.calculateOffset
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.absoluteValue
import kotlin.math.max
import kotlin.math.min
import kotlin.reflect.KClass

@Composable
fun <T : Any> ScatterChart(
    data: List<Scatter<T>>,
    type: KClass<T>,
    modifier: Modifier = Modifier,
    curvedEdges: Boolean = true,
    animationDelay: Long = 300,
    animationMode: AnimationMode = AnimationMode.Together(),
    gridProperties: GridProperties = GridProperties(),
    zeroLineProperties: ZeroLineProperties = ZeroLineProperties(),
    indicatorProperties: HorizontalIndicatorProperties = HorizontalIndicatorProperties(
        textStyle = TextStyle.Default,
        padding = 16.dp
    ),
    labelHelperProperties: LabelHelperProperties = LabelHelperProperties(),
    labelHelperPadding: Dp = 26.dp,
    textMeasurer: TextMeasurer = rememberTextMeasurer(),
    popupProperties: PopupProperties = PopupProperties(
        textStyle = TextStyle.Default.copy(
            color = Color.White,
            fontSize = 12.sp
        )
    ),
    dotsProperties: DotProperties = DotProperties(),
    labels: List<T> = emptyList(),
    labelToString: ((label: T) -> String)? = null,
    labelProperties: LabelProperties = LabelProperties(enabled = false),
    maxValue: Double = data.maxOfOrNull { line -> line.yRange.end } ?: 0.0,
    minValue: Double = max(
        data.minOfOrNull { line -> line.yRange.start } ?: 0.0,
        0.0
    )
) {
    with(LabelConverter.create(type, labelToString)) {
        data.forEach {
            it.xValues = it.data.map { p -> toNumber(p.first).toDouble() }
        }
        LineChart(
            modifier = modifier,
            data = data,
            curvedEdges = curvedEdges,
            animationDelay = animationDelay,
            animationMode = animationMode,
            gridProperties = gridProperties,
            zeroLineProperties = zeroLineProperties,
            indicatorProperties = indicatorProperties,
            labelHelperProperties = labelHelperProperties,
            labelHelperPadding = labelHelperPadding,
            textMeasurer = textMeasurer,
            popupProperties = popupProperties,
            dotsProperties = dotsProperties,
            labelProperties = labelProperties.copy(labels = labels.map { toString(it) }),
            maxValue = maxValue,
            minValue = minValue,
            labelValues = labels.map { toNumber(it).toDouble() }
        )
    }
}

@Composable
fun ValuesChart(
    modifier: Modifier = Modifier,
    data: List<Values>,
    curvedEdges: Boolean = true,
    animationDelay: Long = 300,
    animationMode: AnimationMode = AnimationMode.Together(),
    gridProperties: GridProperties = GridProperties(),
    zeroLineProperties: ZeroLineProperties = ZeroLineProperties(),
    indicatorProperties: HorizontalIndicatorProperties = HorizontalIndicatorProperties(
        textStyle = TextStyle.Default,
        padding = 16.dp
    ),
    labelHelperProperties: LabelHelperProperties = LabelHelperProperties(),
    labelHelperPadding: Dp = 26.dp,
    textMeasurer: TextMeasurer = rememberTextMeasurer(),
    popupProperties: PopupProperties = PopupProperties(
        textStyle = TextStyle.Default.copy(
            color = Color.White,
            fontSize = 12.sp
        )
    ),
    dotsProperties: DotProperties = DotProperties(),
    labelProperties: LabelProperties = LabelProperties(enabled = false),
    maxValue: Double = data.maxOfOrNull { line -> line.yRange.end } ?: 0.0,
    minValue: Double = max(
        data.minOfOrNull { line -> line.yRange.start } ?: 0.0,
        0.0
    )
) {
    val labels = labelProperties.labels
    val labelValues =
        if (labels.isEmpty()) emptyList() else (0..<labels.size).map { it.toDouble() / (labels.size - 1) }
    LineChart(
        modifier = modifier,
        data = data,
        curvedEdges = curvedEdges,
        animationDelay = animationDelay,
        animationMode = animationMode,
        gridProperties = gridProperties,
        zeroLineProperties = zeroLineProperties,
        indicatorProperties = indicatorProperties,
        labelHelperProperties = labelHelperProperties,
        labelHelperPadding = labelHelperPadding,
        textMeasurer = textMeasurer,
        popupProperties = popupProperties,
        dotsProperties = dotsProperties,
        labelProperties = labelProperties,
        maxValue = maxValue,
        minValue = minValue,
        labelValues = labelValues
    )
}

@Composable
private fun <T> LineChart(
    modifier: Modifier = Modifier,
    data: List<Line<T>>,
    curvedEdges: Boolean = true,
    animationDelay: Long = 300,
    animationMode: AnimationMode = AnimationMode.Together(),
    gridProperties: GridProperties = GridProperties(),
    zeroLineProperties: ZeroLineProperties = ZeroLineProperties(),
    indicatorProperties: HorizontalIndicatorProperties = HorizontalIndicatorProperties(
        textStyle = TextStyle.Default,
        padding = 16.dp
    ),
    labelHelperProperties: LabelHelperProperties = LabelHelperProperties(),
    labelHelperPadding: Dp = 26.dp,
    textMeasurer: TextMeasurer = rememberTextMeasurer(),
    popupProperties: PopupProperties = PopupProperties(
        textStyle = TextStyle.Default.copy(
            color = Color.White,
            fontSize = 12.sp
        )
    ),
    dotsProperties: DotProperties = DotProperties(),
    labelValues: List<Double>,
    labelProperties: LabelProperties = LabelProperties(enabled = false),
    maxValue: Double = data.maxOfOrNull { line -> line.yRange.end } ?: 0.0,
    minValue: Double = max(
        data.minOfOrNull { line -> line.yRange.start } ?: 0.0,
        0.0
    )
) {

    val density = LocalDensity.current
    val scope = rememberCoroutineScope()

    var chartSize by remember(density) { mutableStateOf(Size(0f, 0f)) }

    val pathMeasure = remember(chartSize) {
        PathMeasure()
    }

    val popupAnimation = remember(data) {
        Animatable(0f)
    }

    val zeroLineAnimation = remember(data) {
        Animatable(0f)
    }

    val dotAnimators = remember(data) {
        mutableStateListOf<List<Animatable<Float, AnimationVector1D>>>()
    }

    val popups = remember(data) {
        mutableStateListOf<Popup>()
    }
    val popupsOffsetAnimators = remember(chartSize, data) {
        mutableStateListOf<Pair<Animatable<Float, AnimationVector1D>, Animatable<Float, AnimationVector1D>>>()
    }
    val linesPathData = remember(chartSize, data) {
        mutableStateListOf<Path>()
    }

    val yMax = remember(maxValue, indicatorProperties.indicators) {
        val indicatorMax = indicatorProperties.indicators.maxOrNull() ?: return@remember maxValue
        max(maxValue, indicatorMax)
    }
    val yMin = remember(minValue, indicatorProperties.indicators) {
        val indicatorMin = indicatorProperties.indicators.minOrNull() ?: return@remember minValue
        min(minValue, indicatorMin)
    }

    val (xMin, xMax) = remember(data) {
        if (data.isEmpty()) return@remember 0.0 to 0.0
        val head = data.first()
        val range = data.drop(1).fold(head.xRange) { acc, line ->
            acc.copy(
                start = min(acc.start, line.xRange.start),
                end = max(acc.end, line.xRange.end)
            )
        }
        min(range.start, labelValues.min()) to max(range.end, labelValues.max())
    }

    val indicators = remember(indicatorProperties.indicators, yMin, maxValue) {
        indicatorProperties.indicators.ifEmpty {
            split(
                count = indicatorProperties.count,
                minValue = yMin,
                maxValue = yMax
            )
        }
    }

    LaunchedEffect(Unit) {
        if (zeroLineProperties.enabled) {
            zeroLineAnimation.snapTo(0f)
            zeroLineAnimation.animateTo(1f, animationSpec = zeroLineProperties.animationSpec)
        }
    }

    // make animators
    LaunchedEffect(data) {
        dotAnimators.clear()
        data.forEach {
            val animators = mutableListOf<Animatable<Float, AnimationVector1D>>()
            repeat(it.data.size) {
                animators.add(Animatable(0f))
            }
            dotAnimators.add(animators)
        }
    }

    // animate
    LaunchedEffect(data) {
        if (animationMode != AnimationMode.None) delay(animationDelay)

        val animateStroke: suspend (Line<T>) -> Unit = { line ->
            line.strokeProgress.animateTo(1f, animationSpec = line.strokeAnimationSpec)
        }
        val animateGradient: suspend (Line<T>) -> Unit = { line ->
            delay(line.gradientAnimationDelay)
            line.gradientProgress.animateTo(1f, animationSpec = line.gradientAnimationSpec)
        }
        data.forEachIndexed { index, line ->
            when (animationMode) {
                is AnimationMode.OneByOne -> {
                    launch {
                        animateGradient(line)
                    }
                    animateStroke(line)
                }

                is AnimationMode.Together -> {
                    launch {
                        delay(animationMode.delayBuilder(index))
                        animateStroke(line)
                    }
                    launch {
                        delay(animationMode.delayBuilder(index))
                        animateGradient(line)
                    }
                }

                is AnimationMode.None -> {
                    line.gradientProgress.snapTo(1f)
                    line.strokeProgress.snapTo(1f)
                }
            }
        }
    }

    LaunchedEffect(data, yMin, yMax) {
        linesPathData.clear()
    }

    suspend fun hidePopup() {
        popupAnimation.animateTo(0f, animationSpec = tween(300))
        popups.clear()
        popupsOffsetAnimators.clear()
    }

    fun PointerInputScope.showPopup(
        data: List<Line<T>>,
        size: IntSize,
        position: Offset,
        insetPad: InsetPad,
    ) {
        popups.clear()

        data.forEachIndexed { dataIndex, line ->
            val space = Space(
                bounds = insetPad.toBounds(size),
                xMin = xMin,
                xMax = xMax,
                yMin = yMin,
                yMax = yMax,
            )
            with(space) {
                val properties = line.popupProperties ?: popupProperties
                if (!properties.enabled) return@forEachIndexed

                val innerPosition = translateIn(position)
                val positionX = position.x.coerceIn(bounds.left, bounds.right)
                val path = linesPathData[dataIndex]

                val (index, innerOffset, interpolatedInnerOffset) = path.segments.let { segments ->
                    if (segments.isEmpty()) return@forEachIndexed
                    segments.forEachIndexed { index, segment ->
                        val (start, end) = segment.startEnd
                        if (index == 0 && innerPosition.x <= 0)
                            return@let Triple(index, start, start)
                        val t = segment.t(innerPosition.x)
                        if (t != null) {
                            val interpolated = segment.offsetOnSegment(t)
                            if (t < .5) return@let Triple(index - 1, start, interpolated)
                            return@let Triple(index, end, interpolated)
                        }
                    }
                    val last = segments.last().startEnd.second
                    Triple(line.data.size - 1, last, last)
                }


                val isSingleValue = line.data.count() == 1
                val offset = translateOut(innerOffset)

                val showOnPointsThreshold =
                    ((properties.mode as? PopupProperties.Mode.PointMode)?.threshold
                        ?: 0.dp).toPx()
                val meetsThreshold = (offset.x - positionX).absoluteValue <= showOnPointsThreshold

                if (properties.mode !is PopupProperties.Mode.PointMode || meetsThreshold || isSingleValue) {
                    val (relevantOffset, point) =
                        if (properties.mode is PopupProperties.Mode.PointMode)
                            innerOffset to offsetToPoint(innerOffset, true)
                        else interpolatedInnerOffset to offsetToPoint(interpolatedInnerOffset, true)
                    popups.add(
                        Popup(
                            position = relevantOffset,
                            value = point.second,
                            properties = properties,
                            dataIndex = dataIndex,
                            valueIndex = index
                        )
                    )

                    if (popupsOffsetAnimators.count() < popups.count()) {
                        repeat(popups.count() - popupsOffsetAnimators.count()) {
                            popupsOffsetAnimators.add(
                                if (properties.mode is PopupProperties.Mode.PointMode) {
                                    Animatable(relevantOffset.x) to Animatable(relevantOffset.y)
                                } else {
                                    Animatable(0f) to Animatable(0f)
                                }
                            )
                        }
                    }
                }

            }
        }

        scope.launch {
            if (popupAnimation.value != 1f && !popupAnimation.isRunning) {
                popupAnimation.animateTo(1f, animationSpec = popupProperties.animationSpec)
            }
        }
    }

    var onPressJob: Job? = null

    Column(modifier = modifier) {
        if (labelHelperProperties.enabled) {
            data.mapNotNull { line -> line.label?.let { it to line.color } }
                .takeIf { it.isNotEmpty() }
                ?.let {
                    LabelHelper(
                        data = it,
                        properties = labelHelperProperties
                    )
                    Spacer(modifier = Modifier.height(labelHelperPadding))
                }
        }
        Row(modifier = Modifier.fillMaxSize().weight(1f)) {
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                LineChartCanvas(
                    data = data,
                    yMax = yMax,
                    yMin = yMin,
                    xMax = xMax,
                    xMin = xMin,
                    indicators = indicators,
                    indicatorProperties = indicatorProperties,
                    labelValues = labelValues,
                    labelProperties = labelProperties,
                    pathData = linesPathData,
                    popupProperties = popupProperties,
                    scope = scope,
                    hidePopup = { hidePopup() },
                    showPopup = { data, size, position, insetPad ->
                        showPopup(
                            data,
                            size,
                            position,
                            insetPad
                        )
                    },
                    onPressJob = onPressJob,
                    onPressJobChange = { job -> onPressJob = job }
                ) { xTicks, yTicks ->
                    val space = Space(
                        size,
                        xMin = xMin,
                        xMax = xMax,
                        yMin = yMin,
                        yMax = yMax,
                    )
                    drawTicks(
                        xTicks,
                        TickDirection.Vertical,
                        gridProperties.enabled,
                        gridProperties.xAxisProperties
                    )
                    drawTicks(
                        yTicks,
                        TickDirection.Horizontal,
                        gridProperties.enabled,
                        gridProperties.yAxisProperties
                    )
                    val drawZeroLine = {
                        val zeroY = size.height - calculateOffset(
                            minValue = yMin,
                            maxValue = yMax,
                            total = size.height,
                            value = 0f
                        ).toFloat()
                        drawLine(
                            brush = zeroLineProperties.color,
                            start = Offset(x = 0f, y = zeroY),
                            end = Offset(x = size.width * zeroLineAnimation.value, y = zeroY),
                            pathEffect = zeroLineProperties.style.pathEffect,
                            strokeWidth = zeroLineProperties.thickness.toPx()
                        )
                    }
                    if (zeroLineProperties.enabled && zeroLineProperties.zType == ZeroLineProperties.ZType.Under) {
                        drawZeroLine()
                    }
                    if (linesPathData.isEmpty() || linesPathData.count() != data.count()) {
                        data.map {
                            space.pointsToPath(
                                it.doublePairs,
                                rounded = it.curvedEdges ?: curvedEdges
                            )
                        }
                            .also { linesPathData.addAll(it) }
                    }
                    data.forEachIndexed { index, line ->
                        val path = linesPathData.getOrNull(index) ?: return@LineChartCanvas
                        val segmentedPath = Path()
                        pathMeasure.setPath(path, false)
                        pathMeasure.getSegment(
                            0f,
                            pathMeasure.length * line.strokeProgress.value,
                            segmentedPath
                        )
                        var pathEffect: PathEffect? = null
                        val stroke: Float = when (val drawStyle = line.drawStyle) {
                            is DrawStyle.Fill -> {
                                0f
                            }

                            is DrawStyle.Stroke -> {
                                pathEffect = drawStyle.strokeStyle.pathEffect
                                drawStyle.width.toPx()
                            }
                        }
                        clipRect {
                            drawPath(
                                path = segmentedPath,
                                brush = line.color,
                                style = Stroke(width = stroke, pathEffect = pathEffect)
                            )

                            if (line.firstGradientFillColor.isSpecified) {
                                drawLineGradient(
                                    path = path,
                                    color1 = line.firstGradientFillColor,
                                    color2 = line.secondGradientFillColor.takeOrElse { line.firstGradientFillColor },
                                    progress = line.gradientProgress.value,
                                )
                            } else if (line.drawStyle is DrawStyle.Fill) {
                                var fillColor = Color.Unspecified
                                if (line.color is SolidColor) {
                                    fillColor = (line.color as SolidColor).value
                                }
                                drawLineGradient(
                                    path = path,
                                    color1 = fillColor,
                                    color2 = fillColor,
                                    progress = 1f,
                                )
                            }
                        }

                        if ((line.dotProperties?.enabled ?: dotsProperties.enabled)) {
                            drawDots(
                                dataPoints = line.doublePairs.mapIndexed { mapIndex, value ->
                                    DotInfo(
                                        animator = dotAnimators
                                            .getOrNull(index)
                                            ?.getOrNull(mapIndex)
                                            ?: Animatable(0f),
                                        dataIndex = index,
                                        value = value.second.toFloat(),
                                        offset = space.pointToOffset(value)
                                    )
                                },
                                properties = line.dotProperties ?: dotsProperties,
                                linePath = segmentedPath,
                                pathMeasure = pathMeasure,
                                scope = scope,
                            )
                        }
                    }
                    if (zeroLineProperties.enabled && zeroLineProperties.zType == ZeroLineProperties.ZType.Above) {
                        drawZeroLine()
                    }
                    popups.forEachIndexed { index, popup ->
                        drawPopup(
                            popup = popup,
                            nextPopup = popups.getOrNull(index + 1),
                            textMeasurer = textMeasurer,
                            scope = scope,
                            progress = popupAnimation.value,
                            offsetAnimator = popupsOffsetAnimators.getOrNull(index)
                        )
                    }
                }

            }
        }
    }
}


data class MinLabelPadding(
    val minLeft: Float,
    val minRight: Float,
    val minBottom: Float,
    val shouldRotate: Boolean
)

fun getLabelMinPadding(
    labels: List<String>,
    labelProperties: LabelProperties,
    measureLabel: (String) -> TextLayoutResult
): MinLabelPadding {
    if (labels.isEmpty()) return MinLabelPadding(0f, 0f, 0f, false)
    val measuredLabels = labels.map { measureLabel(it) }
    val labelPad = measureLabel("M").size.width
    val maxLabelWidth = measuredLabels.maxOf { it.size.width }
    val minLabelWidth = measuredLabels.minOf { it.size.width }
    val shouldRotate = labelProperties.rotation.mode == LabelProperties.Rotation.Mode.Force ||
            ((maxLabelWidth / minLabelWidth.toDouble()) >= 1.5 && labelProperties.rotation.degree != 0f)
    if (shouldRotate) {
        val rotatedBounds =
            measuredLabels.map { it.bounds.rotate(-labelProperties.rotation.degree) }
        return MinLabelPadding(
            rotatedBounds[0].width / 2f,
            rotatedBounds.last().width / 2f,
            rotatedBounds.maxOf { it.height } + labelPad,
            true
        )
    }
    return MinLabelPadding(
        measuredLabels[0].size.width / 2f,
        measuredLabels.last().size.width / 2f,
        measuredLabels.maxOf { it.size.height }.toFloat() + labelPad,
        false
    )
}

private fun getInsetPad(
    measureLabel: (String) -> TextLayoutResult,
    measureIndicator: (String) -> TextLayoutResult,
    indicators: List<Double>,
    indicatorProperties: HorizontalIndicatorProperties,
    labelProperties: LabelProperties
): InsetPad {
    val labels = labelProperties.labels
    val (minLeft, minRight, minBottom, shouldRotate) =
        getLabelMinPadding(labels, labelProperties, measureLabel)
    val insetPad = InsetPad(0f, 0f, 0f, 0f, shouldRotate)
    if (indicators.isNotEmpty() && indicatorProperties.enabled) {
        val indicatorPad = measureIndicator("M").size.width
        val textIndicators = indicators.map(indicatorProperties.contentBuilder)
        val maxIndicatorWidth = textIndicators.maxOf { measureIndicator(it).size.width }
        val maxIndicatorHeight = textIndicators.maxOf { measureIndicator(it).size.height }
        insetPad.top = maxIndicatorHeight / 2f
        insetPad.bottom = max(maxIndicatorHeight / 2f, minBottom)
        val indicatorSidePad = max(minLeft, maxIndicatorWidth.toFloat()) + indicatorPad.toFloat()
        if (indicatorProperties.position == IndicatorPosition.Horizontal.Start) {
            insetPad.left = indicatorSidePad
            insetPad.right = minRight
        } else {
            insetPad.left = minLeft
            insetPad.right = indicatorSidePad
        }
    } else if (labels.isNotEmpty()) {
        insetPad.left = minLeft
        insetPad.right = minRight
        insetPad.bottom = minBottom
    }
    return insetPad
}

@Composable
private fun <T> RowScope.calculateModifier(
    data: List<Line<T>>,
    yMin: Double,
    yMax: Double,
    pathData: SnapshotStateList<Path>,
    popupProperties: PopupProperties,
    scope: CoroutineScope,
    hidePopup: suspend () -> Unit,
    showPopup: PointerInputScope.(
        data: List<Line<T>>,
        size: IntSize,
        position: Offset
    ) -> Unit,
    onPressJob: Job?,
    onPressJobChange: (Job?) -> Unit
): Modifier {
    return Modifier
        .weight(1f)
        .fillMaxSize()
        .pointerInput(data, yMin, yMax, pathData) {
            if (!popupProperties.enabled || data.all { it.popupProperties?.enabled == false })
                return@pointerInput

            detectHorizontalDragGestures(
                onDragEnd = {
                    scope.launch {
                        hidePopup()
                    }
                },
                onHorizontalDrag = { change, _ ->
                    showPopup(data, size, change.position)
                }
            )
        }
        .pointerInput(Unit) {
            if (!popupProperties.enabled || data.all { it.popupProperties?.enabled == false })
                return@pointerInput

            detectTapGestures(
                onPress = {
                    if (onPressJob?.isActive == true) {
                        onPressJob.cancel()
                        onPressJobChange(null)
                    }

                    onPressJobChange(scope.launch {
                        showPopup(data, size, it)

                        tryAwaitRelease()
                        delay(timeMillis = popupProperties.duration)

                        hidePopup()
                    })
                },
            )
        }
}

@Composable
private fun <T> RowScope.LineChartCanvas(
    data: List<Line<T>>,
    yMax: Double,
    yMin: Double,
    xMax: Double,
    xMin: Double,
    indicators: List<Double>,
    indicatorProperties: HorizontalIndicatorProperties,
    labelValues: List<Double>,
    labelProperties: LabelProperties,
    pathData: SnapshotStateList<Path>,
    popupProperties: PopupProperties,
    scope: CoroutineScope,
    hidePopup: suspend () -> Unit,
    showPopup: PointerInputScope.(
        data: List<Line<T>>,
        size: IntSize,
        position: Offset,
        insetPad: InsetPad
    ) -> Unit,
    onPressJob: Job?,
    onPressJobChange: (Job?) -> Unit,
    insetDrawScope: DrawScope.(xTicks: List<Float>, yTicks: List<Float>) -> Unit,
) {
    val textMeasurer = rememberTextMeasurer()
    val measureIndicator =
        { text: String -> textMeasurer.measure(text, style = indicatorProperties.textStyle) }
    val measureLabel =
        { text: String -> textMeasurer.measure(text, style = labelProperties.textStyle) }
    val insetPad = getInsetPad(
        measureLabel = measureLabel,
        measureIndicator = measureIndicator,
        indicators = indicators,
        indicatorProperties = indicatorProperties,
        labelProperties = labelProperties
    )
    val modifier = calculateModifier(
        data = data,
        yMin = yMin,
        yMax = yMax,
        pathData = pathData,
        popupProperties = popupProperties,
        scope = scope,
        hidePopup = hidePopup,
        showPopup = { data, size, position -> showPopup(data, size, position, insetPad) },
        onPressJob = onPressJob,
        onPressJobChange = onPressJobChange
    )
    Canvas(modifier = modifier) {
        val yTicks = getTicksAndDrawIndicators(
            indicators = indicators,
            indicatorProperties = indicatorProperties,
            minValue = yMin,
            maxValue = yMax,
            measureIndicator = measureIndicator,
            insetPad = insetPad,
            textMeasurer = textMeasurer,
        )
        val xTicks = getTicksAndDrawLabels(
            labelProperties = labelProperties,
            labelValues = labelValues,
            measureLabel = measureLabel,
            insetPad = insetPad,
            textMeasurer = textMeasurer,
            xMin = xMin,
            xMax = xMax,
        )
        inset(insetPad) {
            insetDrawScope(xTicks + listOf(0f, size.width), yTicks + listOf(0f, size.height))
        }
    }
}


