@file:JvmName("KData")

package com.sxtanna.database.ext

import com.sxtanna.database.struct.obj.Sort
import com.sxtanna.database.struct.obj.SqlType
import com.sxtanna.database.struct.obj.Target
import kotlin.reflect.KProperty1

/**
 * Implement data validation for client side failing
 */

const val MIN_UNSIGN = 0

const val TINY_MAX_SIGN = Byte.MAX_VALUE.toInt()
const val TINY_MIN_SIGN = Byte.MIN_VALUE.toInt()
const val TINY_MAX_UNSIGN = 255

const val SMALL_MAX_SIGN = Short.MAX_VALUE.toInt()
const val SMALL_MIN_SIGN = Short.MIN_VALUE.toInt()
const val SMALL_MAX_UNSIGN = 65535

const val MEDIUM_MAX_SIGN = 8388607
const val MEDIUM_MIN_SIGN = -8388608
const val MEDIUM_MAX_UNSIGN = 16777215

const val NORM_MAX_SIGN = Int.MAX_VALUE
const val NORM_MIN_SIGN = Int.MIN_VALUE
const val NORM_MAX_UNSIGN = 4294967295

const val BIG_MAX_SIGN = "9223372036854775807"
const val BIG_MIN_SIGN = "-9223372036854775808"
const val BIG_MAX_UNSIGNED = "18446744073709551615"

const val VARCHAR_SIZE = 255

const val TEXT_SIZE = SMALL_MAX_UNSIGN
const val TINY_TEXT_SIZE = TINY_MAX_UNSIGN
const val MEDIUM_TEXT_SIZE = MEDIUM_MAX_UNSIGN
const val LONG_TEXT_SIZE = NORM_MAX_UNSIGN

/**
 * Represents all rows in a select statement
 */
@get:JvmName("allRows")
val ALL_ROWS = arrayOf("*")
@get:JvmName("noSort")
val NO_SORTS = arrayOf<Sort>()
@get:JvmName("noWhere")
val NO_WHERE = arrayOf<Target>()

typealias Adapter = KProperty1<*, *>.() -> SqlType