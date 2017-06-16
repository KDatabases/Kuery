package com.sxtanna.database.struct.obj

import com.sxtanna.database.struct.obj.SqlType.*
import com.sxtanna.database.struct.obj.SqlType.Char
import java.math.BigInteger
import java.sql.Timestamp
import kotlin.annotation.AnnotationRetention.RUNTIME
import kotlin.annotation.AnnotationTarget.FIELD
import kotlin.annotation.AnnotationTarget.PROPERTY
import kotlin.reflect.KClass

/**
 * Use the type [Char] instead of [VarChar], and use this length
 *
 * **Applicable on [String]**
 *
 * **Max Length is 255**
 */
@Retention(RUNTIME)
@Target(PROPERTY, FIELD)
annotation class Fixed(val length : Int)

/**
 * Instructs the resolver to use a specific type of int server side
 *
 * **Valid types are**
 *   - [TinyInt]
 *   - [SmallInt]
 *   - [MediumInt]
 *   - [NormInt]
 *   - [BigInt]
 *
 * **Applicable on [Byte], [Short], [Int], [Long], and [BigInteger]**
 */
@Retention(RUNTIME)
@Target(PROPERTY, FIELD)
annotation class IntType(val value : KClass<out WholeNumberSqlType<*>>)

/**
 * Instructs the resolver to use a specific type of text server side
 *
 * **Valid types are**
 *   - [TinyText]
 *   - [MediumText]
 *   - [Text]
 *   - [LongText]
 *
 * **Applicable on [String], [Any]**
 */
@Retention(RUNTIME)
@Target(PROPERTY, FIELD)
annotation class TextType(val value : KClass<out TextSqlType>)

/**
 * Mark this field as Nullable server side
 *
 * **Applicable on every type**
 */
@Retention(RUNTIME)
@Target(PROPERTY, FIELD)
annotation class Nullable

/**
 * Mark this field as the primary key of the table
 *
 * **Applicable on every type**
 */
@Retention(RUNTIME)
@Target(PROPERTY, FIELD)
annotation class PrimaryKey

/**
 * Serialize an enum constant as a [VarChar] instead of [EnumSet]
 *
 * **Applicable on Enum Constants**
 */
@Retention(RUNTIME)
@Target(FIELD, PROPERTY)
annotation class Serialized

/**
 * Specify the size of this field, be int max digits, or string length
 *
 * **Applicable on [Byte], [Short], [Int], [Long], [BigInteger], [Float], [Double], and [String]**
 */
@Retention(RUNTIME)
@Target(PROPERTY, FIELD)
annotation class Size(val length : Int = 0, val places : Int = 0)

/**
 * Specify if this should be the current time when added, and if it should update whenever accessed
 *
 * **Applicable on [Timestamp]**
 */
@Retention(RUNTIME)
@Target(FIELD, PROPERTY)
annotation class Time(val current : Boolean = false, val updating : Boolean = false)

/**
 * Specify that this number should be Unsigned
 *
 * **Applicable on [Byte], [Short], [Int], and [Long], [BigInteger]**
 */
@Retention(RUNTIME)
@Target(PROPERTY, FIELD)
annotation class Unsigned