package com.sxtanna.database.type

import com.sxtanna.database.struct.obj.Target
import com.sxtanna.database.struct.obj.Target.Position
import com.sxtanna.database.struct.obj.Target.Position.*
import com.sxtanna.database.struct.obj.Target.Where


@Suppress("UNCHECKED_CAST")
interface Targeted<T : Targeted<T>> { // JetBrains, please allow @JvmOverloads in interfaces...

	val where : MutableList<Target>


	fun like(column : String, value : Any, option : Position, not : Boolean) : T {
		where.add(Target.like(column, value, option, not))
		return this as T
	}

	fun like(column : String, any : Any, option : Position) = like(column, any, option, false)


	fun startsWith(column : String, value : Any, not : Boolean) = like(column, value, START, not)

	fun startsWith(column : String, value : Any) = like(column, value, START, false)


	fun contains(column : String, value : Any, not : Boolean) = like(column, value, CONTAINS, not)

	fun contains(column : String, value : Any) = like(column, value, CONTAINS, false)


	fun endWith(column : String, value : Any, not : Boolean) = like(column, value, END, not)

	fun endWith(column : String, value : Any) = like(column, value, END, false)


	fun equals(column : String, value : Any, not : Boolean) : T {
		where.add(Where.equals(column, value, not))
		return this as T
	}

	fun equals(column : String, value : Any) = equals(column, value, false)


	fun between(column : String, first : Any, second : Any, not : Boolean) : T {
		where.add(Where.between(column, first, second, not))
		return this as T
	}

	fun between(column : String, first : Any, second : Any) = between(column, first, second, false)

	infix fun String.between(data : Pair<Any, Any>) = between(this, data.first, data.second, false)

	infix fun String.notBetween(data : Pair<Any, Any>) = between(this, data.first, data.second, true)


	fun lesser(column : String, value : Any, orEqual : Boolean) : T {
		where.add(Where.lesser(column, value, orEqual))
		return this as T
	}

	infix fun String.lesser(value : Any) = lesser(this, value, false)

	infix fun String.lesserOrEqual(value : Any) = lesser(this, value, true)


	fun greater(column : String, value : Any, orEqual : Boolean) : T {
		where.add(Where.greater(column, value, orEqual))
		return this as T
	}

	infix fun String.greater(value : Any) = greater(this, value, false)

	infix fun String.greaterOrEqual(value : Any) = greater(this, value, true)

}