package com.sxtanna.database.task.builder

import com.sxtanna.database.type.base.SqlObject
import kotlin.reflect.KClass

data class UpdateBuilder<T : SqlObject>(val clazz : KClass<T>, val table : String) : WhereBuilder<T, UpdateBuilder<T>>() {

	val ignoring = mutableSetOf<String>()

	fun ignore(vararg column : String) : UpdateBuilder<T> {
		ignoring.addAll(column.map { it.toLowerCase() })
		return this
	}


	companion object Update {

		@JvmSynthetic
		inline fun <reified T : SqlObject> where(table : String = T::class.simpleName!!) = UpdateBuilder(T::class, table)

		@JvmOverloads
		fun <T : SqlObject> where(clazz : Class<T>, table : String = clazz.simpleName!!) = UpdateBuilder(clazz.kotlin, table)

	}

}