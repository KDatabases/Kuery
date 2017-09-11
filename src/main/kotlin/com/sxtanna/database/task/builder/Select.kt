package com.sxtanna.database.task.builder

import com.sxtanna.database.struct.obj.Sort
import com.sxtanna.database.task.builder.base.TargetedStatement
import com.sxtanna.database.type.Sorted
import com.sxtanna.database.type.base.SqlObject
import kotlin.reflect.KClass

data class Select<T : SqlObject>(val clazz : KClass<T>, override val table : String) : TargetedStatement<T, Select<T>>(), Sorted<Select<T>> {

	override val sorts = mutableListOf<Sort>()


	override fun impl() = this


	companion object {

		inline fun <reified T : SqlObject> from(table : String = T::class.simpleName!!) = Select(T::class, table)

		@JvmStatic
		@JvmOverloads
		fun <T : SqlObject> from(clazz : Class<T>, table : String = clazz.simpleName!!) = Select(clazz.kotlin, table)

	}

}