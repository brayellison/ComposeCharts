package ir.ehsannarmani.compose_charts.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ir.ehsannarmani.compose_charts.models.AnimationMode
import ir.ehsannarmani.compose_charts.models.DotProperties
import ir.ehsannarmani.compose_charts.models.HorizontalIndicatorProperties
import ir.ehsannarmani.compose_charts.models.IndicatorPosition
import ir.ehsannarmani.compose_charts.models.LabelProperties
import ir.ehsannarmani.compose_charts.models.PopupProperties
import ir.ehsannarmani.compose_charts.models.Values
import ir.ehsannarmani.compose_charts.ScatterChart
import ir.ehsannarmani.compose_charts.ValuesChart
import ir.ehsannarmani.compose_charts.models.Scatter

@Composable
fun PhoneSample() {
    LazyColumn(
        modifier = Modifier
            .statusBarsPadding(), verticalArrangement = Arrangement.spacedBy(28.dp),
        contentPadding = PaddingValues(22.dp)
    ) {
        item {
            ChartParent(Modifier) {
                ValuesChart(
                    data = remember {
                        listOf(
                            Values(
                                label = "Test",
                                data = listOf(5.0, 4.0, 3.5, 1.0),
                                color = SolidColor(Color.Red),
                            ),
                            Values(
                                label = "Test 2",
                                data = listOf(2.0, 3.5, 1.0),
                                color = SolidColor(Color.Red),
                                dotProperties = DotProperties(
                                    confirmDraw = {
                                        false
                                    }
                                )
                            ),
                        )
                    },
                    dotsProperties = DotProperties(
                        enabled = true,
                        color = SolidColor(Color.White)
                    ),
                    modifier = Modifier.padding(22.dp),
                    animationMode = AnimationMode.None,
                    minValue = 0.0,
                    maxValue = 7.0,
                    popupProperties = PopupProperties(
                        textStyle = TextStyle.Default.copy(
                            color = Color.White,
                            fontSize = 12.sp
                        ),
                        confirmDraw = {
                            false
                        }
                    ),
                    indicatorProperties = HorizontalIndicatorProperties(
                        indicators = (0..7).map { it.toDouble() } + listOf(5.5),
                        position = IndicatorPosition.Horizontal.End
                    ),
                    labelProperties = LabelProperties(
                        enabled = true,
                        labels = "these labels will rotate".split(' '),
                    )
                )
            }
        }
        item {
            ChartParent(Modifier) {
                ScatterChart(
                    type = Double::class,
                    labels = listOf(0.5, 2.5, 5.0, 7.0),
                    data = remember {
                        listOf(
                            Scatter(
                                label = "Test",
                                data = listOf(
                                    0.0 to 5.0,
                                    0.5 to 4.0,
                                    2.0 to 6.0,
                                    3.0 to 2.0,
                                    5.0 to 5.0,
                                ),
                                color = SolidColor(Color.Red),
                            ),
                            Scatter(
                                label = "Test 2",
                                data = listOf(2.0 to 7.0, 7.0 to 2.0),
                                color = SolidColor(Color.Red),
                                dotProperties = DotProperties(
                                    confirmDraw = {
                                        false
                                    }
                                )
                            ),
                        )
                    },
                    dotsProperties = DotProperties(
                        enabled = true,
                        color = SolidColor(Color.White)
                    ),
                    modifier = Modifier.padding(22.dp),
                    animationMode = AnimationMode.None,
                    minValue = 0.0,
                    maxValue = 7.0,
                    popupProperties = PopupProperties(
                        textStyle = TextStyle.Default.copy(
                            color = Color.White,
                            fontSize = 12.sp
                        ),
                        confirmDraw = {
                            false
                        }
                    ),
                    indicatorProperties = HorizontalIndicatorProperties(
                        indicators = (0..7).map { it.toDouble() } + listOf(5.5),
                        position = IndicatorPosition.Horizontal.End
                    ),
                    labelProperties = LabelProperties(
                        enabled = true,
                    )
                )
            }
        }
        // Pie
        item {
            PieSample()
        }
        item {
            PieSample2()
        }
        item {
            PieSample3()
        }

        // Column
        item {
            ColumnSample()
        }
        item {
            ColumnSample2()
        }
        item {
            ColumnSample3()
        }

        // Row
        item {
            RowSample()
        }
        item {
            RowSample2()
        }
        item {
            RowSample3()
        }

        // Line
        item {
            LineSample()
        }
        item {
            LineSample2()
        }
        item {
            LineSample4()
        }
        item {
            LineSample8()
        }
        item {
            LineSample7()
        }
        item {
            LineSample6()
        }
        item {
            LineSample5()
        }
        item {
            LineSample3()
        }
        item {
            LineSample9()
        }
    }
}