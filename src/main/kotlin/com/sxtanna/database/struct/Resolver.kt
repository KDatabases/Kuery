package com.sxtanna.database.struct

import com.sxtanna.database.ext.*
import com.sxtanna.database.struct.obj.*
import java.math.BigInteger
import java.sql.Timestamp
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
import kotlin.reflect.full.isSubclassOf
import kotlin.reflect.jvm.jvmErasure

object Resolver {

	private val adapters = mutableMapOf<KClass<*>, Adapter>()


	init {
		this[Char::class] = { SqlType.Char(1, isPrimaryKey(), isNotNull()) }
		this[UUID::class] = { SqlType.Char(36, isPrimaryKey(), isNotNull()) }

		this[Boolean::class] = { SqlType.Bool(isPrimaryKey(), isNotNull()) }

		this[Enum::class] = {
			if (isSerialized()) SqlType.VarChar(VARCHAR_SIZE, isPrimaryKey(), isNotNull())
			else {
				@Suppress("UNCHECKED_CAST")
				SqlType.EnumSet(returnType.jvmErasure as KClass<out Enum<*>>, isPrimaryKey(), isNotNull())
			}
		}

		this[Timestamp::class] = {
			val time = findAnnotation<Time>()
			SqlType.Timestamp(time?.current ?: false, time?.updating ?: false, isPrimaryKey(), isNotNull())
		}


		val textAdapter : Adapter = {
			when (findAnnotation<TextType>()?.value) {
				SqlType.TinyText::class -> SqlType.TinyText(isPrimaryKey(), isNotNull())
				SqlType.MediumText::class -> SqlType.MediumText(isPrimaryKey(), isNotNull())
				SqlType.LongText::class -> SqlType.LongText(isPrimaryKey(), isNotNull())
				else -> {
					if (returnType.jvmErasure == String::class) {
						val fix = findAnnotation<Fixed>()?.length ?: -1

						if (fix > 0) SqlType.Char(fix, isPrimaryKey(), isNotNull())
						else SqlType.VarChar(findAnnotation<Size>()?.length ?: VARCHAR_SIZE, isPrimaryKey(), isNotNull())
					} else SqlType.Text(isPrimaryKey(), isNotNull())
				}
			}
		}
		adapters.multiplePut(textAdapter, String::class, Any::class)

		val numberAdapter : Adapter = {

			val size = findAnnotation<Size>()
			val length = (size?.length ?: 100)

			when (findAnnotation<IntType>()?.value) {
				SqlType.TinyInt::class -> SqlType.TinyInt(length.coerceAtMost(TINY_MAX_UNSIGN), isUnsigned(), isPrimaryKey(), isNotNull())
				SqlType.SmallInt::class -> SqlType.SmallInt(length.coerceAtMost(SMALL_MAX_UNSIGN), isUnsigned(), isPrimaryKey(), isNotNull())
				SqlType.MediumInt::class -> SqlType.MediumInt(length.coerceAtMost(MEDIUM_MAX_UNSIGN), isUnsigned(), isPrimaryKey(), isNotNull())
				SqlType.BigInt::class -> SqlType.BigInt(length.toString(), isUnsigned(), isPrimaryKey(), isNotNull())
				else -> {
					if (returnType.jvmErasure == BigInteger::class || returnType.jvmErasure == Long::class)  {
						SqlType.BigInt(length.toString(), isUnsigned(), isPrimaryKey(), isNotNull())
					}
					else SqlType.NormInt(length.toLong().coerceAtMost(NORM_MAX_UNSIGN), isUnsigned(), isPrimaryKey(), isNotNull())
				}
			}
		}
		adapters.multiplePut(numberAdapter, Byte::class, Short::class, Int::class, Long::class, BigInteger::class)

		val decimalAdapter : Adapter = {
			val size = findAnnotation<Size>()
			SqlType.Decimal(size?.length ?: 10, size?.places ?: 1, isPrimaryKey(), isNotNull())
		}
		adapters.multiplePut(decimalAdapter, Float::class, Double::class)
	}


	operator fun set(type : KClass<*>, adapter : Adapter) {
		adapters[type] = adapter
	}

	operator fun get(property : KProperty1<*, *>) : SqlType {
		val type = property.returnType.jvmErasure
		val adapter = checkNotNull(adapters[type] ?: adapters[if (type.isEnum()) Enum::class else Any::class])

		return checkNotNull(adapter) { "Random Impossible error... but for type $type" }.invoke(property)
	}


	private fun KProperty1<*, *>.isNotNull() = findAnnotation<Nullable>() == null && returnType.isMarkedNullable.not()

	private fun KProperty1<*, *>.isUnsigned() = findAnnotation<Unsigned>() != null

	private fun KProperty1<*, *>.isSerialized() = findAnnotation<Serialized>() != null

	private fun KProperty1<*, *>.isPrimaryKey() = findAnnotation<PrimaryKey>() != null


	private fun KClass<*>.isEnum() = this.isSubclassOf(Enum::class)

	private fun <K, V> MutableMap<K, V>.multiplePut(value : V, vararg keys : K) = keys.forEach { put(it, value) }

}