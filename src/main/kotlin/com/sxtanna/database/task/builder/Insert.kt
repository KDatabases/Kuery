package com.sxtanna.database.task.builder

import com.sxtanna.database.ext.PrimaryKey
import com.sxtanna.database.task.builder.base.BuilderStatement
import com.sxtanna.database.type.base.SqlObject
import kotlin.reflect.KClass
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.memberProperties

class Insert<T : SqlObject>(val clazz : KClass<T>, override val table : String) : BuilderStatement() {

	var ignore = false
		private set
	var update = emptyArray<String>()
		private set


	fun onDupeIgnore() : Insert<T> {
		ignore = true
		return this
	}

	@JvmOverloads
	fun onDupeUpdate(vararg column : CharSequence = arrayOf(clazz.memberProperties.find { it.findAnnotation<PrimaryKey>() != null }?.name ?: "")) : Insert<T> {
		update = column.map { it.toString() }.toTypedArray()
		return this
	}


	companion object {

		inline fun <reified T : SqlObject> into(table : String = T::class.simpleName!!) = Insert(T::class, table)

		@JvmStatic
		@JvmOverloads
		fun <T : SqlObject> into(clazz : Class<T>, table : String = clazz.simpleName!!) = Insert(clazz.kotlin, table)

	}

}