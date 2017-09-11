@file:JvmName("Kext")

package com.sxtanna.database.ext

import com.sxtanna.database.struct.base.Creator
import com.sxtanna.database.struct.base.Duo
import com.sxtanna.database.struct.obj.Sort
import com.sxtanna.database.struct.obj.Target
import com.sxtanna.database.task.builder.Create
import com.sxtanna.database.task.builder.Insert
import com.sxtanna.database.task.builder.Select
import com.sxtanna.database.task.builder.Update
import com.sxtanna.database.type.base.SqlObject
import java.sql.ResultSet
import java.util.function.Function
import java.util.function.Supplier


@JvmSynthetic
fun <O> attempt(catch : Boolean = false, block : () -> O) : O? {
	try {
		return block()
	}
	catch (e : Exception) {}
	return null
}

@JvmOverloads
fun <O> attempt(catch : Boolean = false, block : Supplier<O>) : O? = attempt(catch) { block.get() }


fun Boolean.value(input : String?) = if (this) input ?: "" else ""

/**
 * Special infix function for [Duo]
 *
 * @param value The value to into with this Duo
 * @sample createColumns
 */
infix fun <T> String.co(value : T) = Duo(this, value)


fun <O : SqlObject> create(clazz : Class<O>, creator : Function<ResultSet, O>) = object : Creator<O>(clazz.kotlin) {
	override fun apply(t : ResultSet) : O = creator.apply(t)
}


fun data(vararg any : Any) = any

fun sorts(vararg sorts : Sort) = sorts

fun targets(vararg targets : Target) = targets


//region Build Function Creators
@JvmSynthetic
inline fun <reified T : SqlObject> createTable(name : String = T::class.simpleName!!) = Create.from<T>(name)

@JvmSynthetic
inline fun <reified T : SqlObject> selectFrom(table : String = T::class.simpleName!!, noinline block : Select<T>.() -> Unit = {}) = Select(T::class, table).apply(block)

@JvmSynthetic
inline fun <reified T : SqlObject> insertInto(table : String = T::class.simpleName!!, noinline block : Insert<T>.() -> Unit = {}) = Insert(T::class, table).apply(block)

@JvmSynthetic
inline fun <reified T : SqlObject> updateIn(table : String = T::class.simpleName!!, noinline block : Update<T>.() -> Unit = {}) = Update(T::class, table).apply(block)
//endregion


private fun createColumns() = Duo.valueColumns("One" co 1, "Two" co 2, "True" co true)

