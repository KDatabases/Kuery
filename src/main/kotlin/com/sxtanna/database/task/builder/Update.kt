package com.sxtanna.database.task.builder

import com.sxtanna.database.task.builder.base.TargetedStatement
import com.sxtanna.database.type.base.SqlObject
import kotlin.reflect.KClass

data class Update<T : SqlObject>(val clazz : KClass<T>, override val table : String) : TargetedStatement<T, Update<T>>() {

	val ignoring = mutableSetOf<String>()

	fun ignore(vararg column : String) : Update<T> {
		ignoring.addAll(column.map { it.toLowerCase() })
		return this
	}


	override fun impl() = this


	companion object {

		inline fun <reified T : SqlObject> where(table : String = T::class.simpleName!!) = Update(T::class, table)

		@JvmStatic
		@JvmOverloads
		fun <T : SqlObject> where(clazz : Class<T>, table : String = clazz.simpleName!!) = Update(clazz.kotlin, table)

	}

}