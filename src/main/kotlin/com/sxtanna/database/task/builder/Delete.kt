package com.sxtanna.database.task.builder

import com.sxtanna.database.task.builder.base.TargetedStatement
import com.sxtanna.database.type.base.SqlObject
import kotlin.reflect.KClass

data class Delete<T : SqlObject>(val clazz : KClass<T>, override val table : String) : TargetedStatement<T, Delete<T>>() {

	override fun impl() = this


    companion object {

        inline fun <reified T : SqlObject> from(table : String = T::class.simpleName!!) = Delete(T::class, table)

        @JvmStatic
        @JvmOverloads
        fun <T : SqlObject> from(clazz : Class<T>, table : String = clazz.simpleName!!) = Delete(clazz.kotlin, table)

    }

}