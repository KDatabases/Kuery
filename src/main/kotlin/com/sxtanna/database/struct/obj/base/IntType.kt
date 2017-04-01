package com.sxtanna.database.struct.obj.base

import com.sxtanna.database.struct.SqlType.WholeNumberSqlType
import kotlin.annotation.AnnotationRetention.RUNTIME
import kotlin.annotation.AnnotationTarget.FIELD
import kotlin.annotation.AnnotationTarget.PROPERTY
import kotlin.reflect.KClass

@Retention(RUNTIME)
@Target(PROPERTY, FIELD)
annotation class IntType(val value : KClass<out WholeNumberSqlType>)