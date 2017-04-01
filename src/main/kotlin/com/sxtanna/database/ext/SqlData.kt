package com.sxtanna.database.ext

const val MIN_UNSIGN = 0L

const val TINY_MAX_SIGN = Byte.MAX_VALUE.toLong()
const val TINY_MIN_SIGN = Byte.MIN_VALUE.toLong()
const val TINY_MAX_UNSIGN = 255L

const val SMALL_MAX_SIGN = Short.MAX_VALUE.toLong()
const val SMALL_MIN_SIGN = Short.MIN_VALUE.toLong()
const val SMALL_MAX_UNSIGN = 65535L

const val MEDIUM_MAX_SIGN = 8388607L
const val MEDIUM_MIN_SIGN = -8388608L
const val MEDIUM_MAX_UNSIGN = 16777215L

const val NORM_MAX_SIGN = Int.MAX_VALUE.toLong()
const val NORM_MIN_SIGN = Int.MIN_VALUE.toLong()
const val NORM_MAX_UNSIGN = 4294967295L


const val VARCHAR_SIZE = 255

/**
 * Represents all rows in a select statement
 */
val ALL_ROWS = arrayOf("*")

