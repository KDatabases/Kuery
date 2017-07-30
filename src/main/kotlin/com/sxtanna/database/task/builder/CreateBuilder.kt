package com.sxtanna.database.task.builder

import com.sxtanna.database.ext.co
import com.sxtanna.database.struct.Resolver
import com.sxtanna.database.struct.base.Duo
import com.sxtanna.database.struct.obj.SqlType
import com.sxtanna.database.type.base.SqlObject
import kotlin.reflect.full.declaredMemberProperties

data class CreateBuilder(val table : String) {

	val columns = mutableListOf<Duo<SqlType>>()
	private var inited = false


	fun co(name : String, type : SqlType) = apply { columns.add(name co type) }

	@JvmSynthetic
	fun co(name : String, type : () -> SqlType) = apply { columns.add(name co type()) }


	fun cos(vararg columns : Duo<SqlType>) = apply { columns.forEach { co(it.name, it.value) } }


	@JvmSynthetic
	inline fun <reified T : SqlObject> init() = init(T::class.java)

	fun <T : SqlObject> init(clazz : Class<T>) = apply {
		if (inited) return@apply

		columns.addAll(clazz.kotlin.declaredMemberProperties.map { Duo(it.name, Resolver[it]) })
		inited = true
	}


	companion object Create {

		fun table(table : String) = CreateBuilder(table)

		@JvmSynthetic
		inline fun <reified T : SqlObject> from(table : String = T::class.simpleName!!) = CreateBuilder(table).init<T>()

		@JvmOverloads
		fun <T : SqlObject> from(clazz : Class<T>, table : String = clazz.simpleName!!) = CreateBuilder(table).init(clazz)

	}

}