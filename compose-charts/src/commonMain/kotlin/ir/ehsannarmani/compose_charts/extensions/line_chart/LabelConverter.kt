package ir.ehsannarmani.compose_charts.extensions.line_chart

import ir.ehsannarmani.compose_charts.extensions.format
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atTime
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import kotlin.reflect.KClass
import kotlin.time.Instant


sealed interface LabelConverter<T> {
    fun toNumber(label: T): Number
    fun toString(label: T): String
    fun toLabel(number: Number): T

    @Suppress("UNCHECKED_CAST")
    companion object {
        fun <T : Any> create(type: KClass<T>, labelToString: ((T) -> String)? = null): LabelConverter<T> {
            return when (type) {
                Double::class -> DoubleConverter(labelToString as ((Double) -> String)?)
                Float::class -> FloatConverter(labelToString as ((Float) -> String)?)
                Int::class -> IntConverter(labelToString as ((Int) -> String)?)
                Long::class -> LongConverter(labelToString as ((Long) -> String)?)
                Instant::class -> InstantConverter(labelToString as ((Instant) -> String)?)
                LocalDateTime::class -> LocalDateTimeConverter(labelToString as ((LocalDateTime) -> String)?)
                LocalDate::class -> LocalDateConverter(labelToString as ((LocalDate) -> String)?)
                LocalTime::class -> LocalTimeConverter(labelToString as ((LocalTime) -> String)?)
                else -> throw Exception("Unhandled type `${type}`. See implemented types.")
            } as LabelConverter<T>
        }
    }
}

// NUMBER CONVERTERS
class DoubleConverter(val labelToString: ((Double) -> String)?) : LabelConverter<Double> {
    override fun toNumber(label: Double) = label
    override fun toString(label: Double) = labelToString?.invoke(label) ?: label.format(1)
    override fun toLabel(number: Number) = number.toDouble()
}

class FloatConverter(val labelToString: ((Float) -> String)?) : LabelConverter<Float> {
    override fun toNumber(label: Float) = label
    override fun toString(label: Float) = labelToString?.invoke(label) ?: label.toDouble().format(1)
    override fun toLabel(number: Number) = number.toFloat()
}

class IntConverter(val labelToString: ((Int) -> String)?) : LabelConverter<Int> {
    override fun toNumber(label: Int) = label
    override fun toString(label: Int) = labelToString?.invoke(label) ?: label.toString()
    override fun toLabel(number: Number) = number.toInt()
}

class LongConverter(val labelToString: ((Long) -> String)?) : LabelConverter<Long> {
    override fun toNumber(label: Long) = label
    override fun toString(label: Long) = labelToString?.invoke(label) ?: label.toString()
    override fun toLabel(number: Number) = number.toLong()
}

// DATE/TIME/DATETIME CONVERTERS

class InstantConverter(val labelToString: ((Instant) -> String)?) : LabelConverter<Instant> {
    val timeZone = TimeZone.currentSystemDefault()
    override fun toNumber(label: Instant) = label.toEpochMilliseconds()
    override fun toString(label: Instant) =
        labelToString?.invoke(label) ?: label.toLocalDateTime(timeZone).toString()

    override fun toLabel(number: Number) = Instant.fromEpochMilliseconds(number.toLong())
}

class LocalDateTimeConverter(val labelToString: ((LocalDateTime) -> String)?) :
    LabelConverter<LocalDateTime> {
    val timeZone = TimeZone.currentSystemDefault()
    override fun toNumber(label: LocalDateTime) = label.toInstant(timeZone).toEpochMilliseconds()
    override fun toString(label: LocalDateTime) =
        labelToString?.invoke(label) ?: label.toString()

    override fun toLabel(number: Number) =
        Instant.fromEpochMilliseconds(number.toLong()).toLocalDateTime(timeZone)
}

class LocalDateConverter(val labelToString: ((LocalDate) -> String)?) :
    LabelConverter<LocalDate> {
    val timeZone = TimeZone.currentSystemDefault()
    override fun toNumber(label: LocalDate) = label.atTime(0, 0).toInstant(timeZone).toEpochMilliseconds()
    override fun toString(label: LocalDate) =
        labelToString?.invoke(label) ?: label.toString()

    override fun toLabel(number: Number) =
        Instant.fromEpochMilliseconds(number.toLong()).toLocalDateTime(timeZone).date
}

class LocalTimeConverter(val labelToString: ((LocalTime) -> String)?) :
    LabelConverter<LocalTime> {
    override fun toNumber(label: LocalTime) = label.toMillisecondOfDay()
    override fun toString(label: LocalTime) =
        labelToString?.invoke(label) ?: label.toString()

    override fun toLabel(number: Number) =
        LocalTime.fromMillisecondOfDay(number.toInt())
}
