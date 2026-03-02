package ir.ehsannarmani.compose_charts.extensions

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathSegment


val SEGMENT_TYPES = listOf(
    PathSegment.Type.Line,
    PathSegment.Type.Quadratic,
    PathSegment.Type.Cubic,
    PathSegment.Type.Move
)

fun Path.valueOnPath(x: Float): Float? {
    for (segment in this) {
        if (segment.type in SEGMENT_TYPES) {
            val t = segment.t(x)
            if (t != null) return segment.offsetOnSegment(t).y
        }
    }
    return null
}
val Path.segments: List<PathSegment>
    get() {
        val out = mutableListOf<PathSegment>()
        for (next in this) out.add(next)
        return out.toList()
    }

val Path.startEnd: Pair<Offset, Offset>
    get() {
        val segments = segments
        return segments[0].startEnd.first to
            segments.last().startEnd.second
    }


val PathSegment.startEnd: Pair<Offset, Offset>
    get() {
        val controlPoints = controlPoints
        return controlPoints.first() to controlPoints.last()
    }

fun PathSegment.t(x: Float): Float? {
    val xMin = points[0]
    val xMax = points[points.size - 2]
    if (x in xMin..xMax) return (x - xMin) / (xMax - xMin)
    return null
}

val PathSegment.controlPoints: List<Offset>
    get() = (0..<points.size / 2)
        .map { Offset(points[it * 2], points[it * 2 + 1]) }

fun PathSegment.offsetOnSegment(t: Float): Offset {
    return controlPoints.lerpUntil(t)
}

fun Offset.lineTo(other: Offset): (Float) -> Offset {
    return { t: Float -> this * (1 - t) + other * t }
}

fun List<Offset>.lerpUntil(t: Float): Offset {
    if (this.isEmpty()) throw Exception("Must apply to array with `size >= 1`")
    var last = this.first()
    if (this.size == 1) return last
    val tail = this.drop(1)
    val out = tail.fold(emptyList<Offset>()) { acc, next ->
        val lerped = last.lineTo(next)(t)
        last = next
        acc + listOf(lerped)
    }
    return out.lerpUntil(t)
}

fun Offset.cubicTo(other: Offset): (Float) -> Offset {
    val xMid = (this.x + other.x) / 2
    val c1 = this.copy(x = xMid)
    val c2 = other.copy(x = xMid)
    return { t: Float ->
        val tt = t * t
        val ttt = tt * t
        val u = 1 - t
        val uu = u * u
        val uuu = uu * u
        this * uuu + c1 * t * uu * 3f + c2 * tt * u * 3f + other * ttt
    }
}

