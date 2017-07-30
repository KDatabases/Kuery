package com.sxtanna.database.task.builder

import com.sxtanna.database.struct.obj.Sort
import com.sxtanna.database.type.Sorted
import com.sxtanna.database.type.base.SqlObject
import kotlin.reflect.KClass

data class SelectBuilder<T : SqlObject>(val clazz : KClass<T>, val table : String) : WhereBuilder<T, SelectBuilder<T>>(), Sorted<SelectBuilder<T>> {

	override val sorts = mutableListOf<Sort>()

	companion object Select {

		@JvmSynthetic
		inline fun <reified T : SqlObject> from(table : String = T::class.simpleName!!) = SelectBuilder(T::class, table)

		@JvmOverloads
		fun <T : SqlObject> from(clazz : Class<T>, table : String = clazz.simpleName!!) = SelectBuilder(clazz.kotlin, table)

	}

}