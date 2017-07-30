package com.sxtanna.database.task.builder

import com.sxtanna.database.ext.PrimaryKey
import com.sxtanna.database.type.base.SqlObject
import kotlin.reflect.KClass
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.memberProperties

class InsertBuilder<T : SqlObject>(val clazz : KClass<T>, val table : String) {

	var ignore = false
		private set
	var update = emptyArray<String>()
		private set


	fun onDupeIgnore() : InsertBuilder<T> {
		ignore = true
		return this
	}

	@JvmOverloads
	fun onDupeUpdate(vararg column : CharSequence = arrayOf(clazz.memberProperties.find { it.findAnnotation<PrimaryKey>() != null }?.name ?: "")) : InsertBuilder<T> {
		update = column.map { it.toString() }.toTypedArray()
		return this
	}


	companion object Insert {

		@JvmSynthetic
		inline fun <reified T : SqlObject> into(table : String = T::class.simpleName!!) = InsertBuilder(T::class, table)

		@JvmOverloads
		fun <T : SqlObject> into(clazz : Class<T>, table : String = clazz.simpleName!!) = InsertBuilder(clazz.kotlin, table)

	}

}