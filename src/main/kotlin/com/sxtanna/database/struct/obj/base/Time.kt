package com.sxtanna.database.struct.obj.base

import kotlin.annotation.AnnotationRetention.RUNTIME
import kotlin.annotation.AnnotationTarget.FIELD
import kotlin.annotation.AnnotationTarget.PROPERTY

@Retention(RUNTIME)
@Target(FIELD, PROPERTY)
annotation class Time(val current : Boolean = false, val updating : Boolean = false)