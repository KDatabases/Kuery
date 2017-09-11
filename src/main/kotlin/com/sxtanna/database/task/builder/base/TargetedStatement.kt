package com.sxtanna.database.task.builder.base

import com.sxtanna.database.struct.obj.Target
import com.sxtanna.database.struct.obj.Target.Position
import com.sxtanna.database.struct.obj.Target.Position.*
import com.sxtanna.database.type.base.SqlObject

abstract class TargetedStatement<T : SqlObject, W : TargetedStatement<T, W>> : BuilderStatement() {

	val where = mutableListOf<Target>()


	@JvmOverloads
	fun like(column : String, value : Any, option : Position, not : Boolean = false) = impl().apply { where.add(Target.like(column, value, option, not)) }


	@JvmOverloads
	fun startsWith(column : String, value : Any, not : Boolean = false) = like(column, value, START, not)

	@JvmSynthetic
	@JvmName("inStartsWith")
	infix fun String.startsWith(value : Any) = startsWith(this, value, false)

	@JvmSynthetic
	infix fun String.startsNotWith(value : Any) = startsWith(this, value, true)


	@JvmOverloads
	fun contains(column : String, value : Any, not : Boolean = false) = like(column, value, CONTAINS, not)

	@JvmSynthetic
	@JvmName("inContains")
	infix fun String.contains(value : Any) = contains(this, value, false)

	@JvmSynthetic
	infix fun String.containsNot(value : Any) = contains(this, value, true)


	@JvmOverloads
	fun endsWith(column : String, value : Any, not : Boolean = false) = like(column, value, END, not)

	@JvmSynthetic
	@JvmName("inEndsWith")
	infix fun String.endsWith(value : Any) = endsWith(this, value, false)

	@JvmSynthetic
	infix fun String.endsNotWith(value : Any) = endsWith(this, value, true)


	@JvmOverloads
	fun equals(column : String, value : Any, not : Boolean = false) = impl().apply { where.add(Target.equals(column, value, not)) }

	@JvmSynthetic
	@JvmName("inEquals")
	infix fun String.equals(value : Any) = equals(this, value, false)

	@JvmSynthetic
	infix fun String.equalsNot(value : Any) = equals(this, value, true)


	@JvmOverloads
	fun between(column : String, first : Any, second : Any, not : Boolean = false) = impl().apply { where.add(Target.between(column, first, second, not)) }

	@JvmSynthetic
	infix fun String.between(data : Pair<Any, Any>) = between(this, data.first, data.second, false)

	@JvmSynthetic
	infix fun String.notBetween(data : Pair<Any, Any>) = between(this, data.first, data.second, true)


	fun lesser(column : String, value : Any, orEqual : Boolean) = impl().apply { where.add(Target.lesser(column, value, orEqual)) }

	@JvmSynthetic
	infix fun String.lesser(value : Any) = lesser(this, value, false)

	@JvmSynthetic
	infix fun String.lesserOrEqual(value : Any) = lesser(this, value, true)


	fun greater(column : String, value : Any, orEqual : Boolean) = impl().apply { where.add(Target.greater(column, value, orEqual)) }

	@JvmSynthetic
	infix fun String.greater(value : Any) = greater(this, value, false)

	@JvmSynthetic
	infix fun String.greaterOrEqual(value : Any) = greater(this, value, true)


	abstract protected fun impl() : W

}