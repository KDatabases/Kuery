package com.sxtanna.database.struct.obj

import com.sxtanna.database.ext.VARCHAR_SIZE
import com.sxtanna.database.struct.SqlType
import com.sxtanna.database.struct.SqlType.*
import com.sxtanna.database.struct.SqlType.EnumSet
import com.sxtanna.database.struct.obj.base.*
import java.util.*
import kotlin.Byte
import kotlin.Double
import kotlin.Enum
import kotlin.Float
import kotlin.Int
import kotlin.Long
import kotlin.Short
import kotlin.String
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.jvm.jvmErasure

@Suppress("UNCHECKED_CAST")
object SqlProperty {

	private val adapters = mutableMapOf<KClass<*>, KProperty1<*, *>.() -> SqlType>()
	private val fallback : KProperty1<*, *>.() -> SqlType = { VarChar(VARCHAR_SIZE, isPrimaryKey(), isNotNull()) }

	init {
		adapters[Boolean::class] = { Bool(isPrimaryKey(), isNotNull()) }

		adapters[Char::class] = { SqlType.Char(1, isPrimaryKey(), isNotNull()) }
		adapters[UUID::class] = { SqlType.Char(36, isPrimaryKey(), isNotNull()) }

		adapters[Enum::class] = { EnumSet(returnType.jvmErasure as KClass<out Enum<*>>, isPrimaryKey(), isNotNull()) }
		adapters[String::class] = { VarChar(findAnnotation<Size>()?.length ?: VARCHAR_SIZE, isPrimaryKey(), isNotNull()) }


		val numberAdapter : KProperty1<*, *>.() -> SqlType = {
			val size = findAnnotation<Size>()?.length ?: 100
			when(findAnnotation<IntType>()?.value ?: TinyInt::class) {
				TinyInt::class -> TinyInt(size, isUnsigned(), isPrimaryKey(), isNotNull())
				SmallInt::class -> SmallInt(size, isUnsigned(), isPrimaryKey(), isNotNull())
				MediumInt::class -> MediumInt(size, isUnsigned(), isPrimaryKey(), isNotNull())
				else -> NormInt(size.toLong(), isUnsigned(), isPrimaryKey(), isNotNull())
			}
		}

		adapters.multiplePut(numberAdapter, Byte::class, Short::class, Int::class, Long::class)

		val decimalAdapter : KProperty1<*, *>.() -> SqlType = {
			val size = findAnnotation<Size>()
			Decimal(size?.length ?: 10, size?.places ?: 1, isPrimaryKey(), isNotNull())
		}

		adapters.multiplePut(decimalAdapter, Float::class, Double::class)
	}


	operator fun get(property : KProperty1<*, *>) : SqlType = (adapters[property.returnType.jvmErasure] ?: fallback).invoke(property)


	private fun KProperty1<*, *>.isNotNull() = findAnnotation<Nullable>() == null

	private fun KProperty1<*, *>.isUnsigned() = findAnnotation<Unsigned>() != null

	private fun KProperty1<*, *>.isPrimaryKey() = findAnnotation<PrimaryKey>() != null

	private fun <K, V> MutableMap<K, V>.multiplePut(value : V, vararg keys : K) = keys.forEach { put(it, value) }

}