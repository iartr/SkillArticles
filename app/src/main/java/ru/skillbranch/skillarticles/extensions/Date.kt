package ru.skillbranch.skillarticles.extensions

import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.absoluteValue

const val SECOND = 1000L
const val MINUTE = 60 * SECOND
const val HOUR = 60 * MINUTE
const val DAY = 24 * HOUR

val Int.sec get() = this * SECOND
val Int.min get() = this * MINUTE
val Int.hour get() = this * HOUR
val Int.day get() = this * DAY

fun Date.format(pattern: String = "HH:mm:ss dd.MM.yy"): String {
    val dateFormat = SimpleDateFormat(pattern, Locale("ru"))
    return dateFormat.format(this)
}

fun Date.shortFormat(): String {
    val pattern = if(this.isSameDay(Date())) "HH:mm" else "dd.MM.yy"
    val dateFormat = SimpleDateFormat(pattern, Locale("ru"))
    return  dateFormat.format(this)
}

fun Date.isSameDay(date: Date): Boolean {
    val day1 = this.time / DAY
    val day2 = date.time / DAY
    return  day1 == day2
}

fun Date.add(value: Int, units: TimeUnits = TimeUnits.SECOND): Date {
    var time = this.time

    time += when (units) {
        TimeUnits.SECOND -> value * SECOND
        TimeUnits.MINUTE -> value * MINUTE
        TimeUnits.HOUR -> value * HOUR
        TimeUnits.DAY -> value * DAY
    }
    this.time = time
    return this
}

fun Date.humanizeDiff(date: Date = Date()): String {
    val duration = ((date.time - 200)/1000 - (this.time - 200)/1000) * 1000

    return if (duration >= 0) {
        when (duration) {
            in 0.sec..1.sec -> "только что"
            in 2.sec..45.sec -> "несколько секунд назад"
            in 46.sec..75.sec -> "минуту назад"
            in 76.sec..45.min -> "${TimeUnits.MINUTE.plural((duration.absoluteValue / MINUTE).toInt())} назад"
            in 46.min..75.min -> "час назад"
            in 76.min..22.hour -> "${TimeUnits.HOUR.plural((duration.absoluteValue / HOUR).toInt())} назад"
            in 23.hour..26.hour -> "день назад"
            in 27.hour..360.day -> "${TimeUnits.DAY.plural((duration.absoluteValue / DAY).toInt())} назад"
            else -> "более года назад"
        }
    } else {
        when (duration) {
            in (-45).sec..0.sec -> "через несколько секунд"
            in (-75).sec..(-45).sec -> "через минуту"
            in (-45).min..(-75).sec -> "через ${TimeUnits.MINUTE.plural((duration.absoluteValue / MINUTE).toInt())}"
            in (-75).min..(-45).min -> "через час"
            in (-22).hour..(-75).min -> "через ${TimeUnits.HOUR.plural((duration.absoluteValue / HOUR).toInt())}"
            in (-26).hour..(-22).hour -> "через день"
            in (-360).day..(-26).hour -> "через ${TimeUnits.DAY.plural((duration.absoluteValue / DAY).toInt())}"
            else -> "более чем через год"
        }
    }
}


enum class TimeUnits {
    SECOND,
    MINUTE,
    HOUR,
    DAY;
    fun plural(value: Int): String {
        return when(this) {
            SECOND -> secondAsPlurals(value * 1L)
            MINUTE -> minuteAsPlurals(value * 1L)
            HOUR -> hourAsPlurals(value * 1L)
            DAY -> dayAsPlurals(value * 1L)
        }
    }
}

private fun secondAsPlurals(value: Long) = when(value.asPlurals) {
    Plurals.ONE -> "$value секунду"
    Plurals.FEW -> "$value секунды"
    Plurals.MANY -> "$value секунд"
}

private fun minuteAsPlurals(value: Long) = when(value.asPlurals) {
    Plurals.ONE -> "$value минуту"
    Plurals.FEW -> "$value минуты"
    Plurals.MANY -> "$value минут"
}

private fun hourAsPlurals(value: Long) = when(value.asPlurals) {
    Plurals.ONE -> "$value час"
    Plurals.FEW -> "$value часа"
    Plurals.MANY -> "$value часов"
}

private fun dayAsPlurals(value: Long) = when(value.asPlurals) {
    Plurals.ONE -> "$value день"
    Plurals.FEW -> "$value дня"
    Plurals.MANY -> "$value дней"
}

val Long.asPlurals
    get() = when {

        this % 100L in 5L..20L -> Plurals.MANY
        this % 10L == 1L -> Plurals.ONE
        this % 10L in 2L..4L -> Plurals.FEW

        else -> Plurals.MANY
    }

enum class Plurals {
    ONE,
    FEW,
    MANY
}