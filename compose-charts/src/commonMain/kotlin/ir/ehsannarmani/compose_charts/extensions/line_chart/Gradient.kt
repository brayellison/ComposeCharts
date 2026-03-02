package ir.ehsannarmani.compose_charts.extensions.line_chart

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import ir.ehsannarmani.compose_charts.extensions.startEnd


internal fun DrawScope.drawLineGradient(
    path: Path,
    color1: Color,
    color2: Color,
    progress: Float
) {
    drawIntoCanvas {
        val (start, end) = path.startEnd
        val p = Path()
        p.addPath(path)
        p.lineTo(end.x, size.height)
        p.lineTo(start.x, size.height)
        p.close()
        val paint = Paint()
        paint.shader = LinearGradientShader(
            Offset(0f, 0f),
            Offset(0f, size.height),
            listOf(
                color1.copy(alpha = color1.alpha * progress),
                color2.copy(alpha = color2.alpha * progress),
            ),
            tileMode = TileMode.Mirror
        )
        it.drawPath(p, paint)
    }
}