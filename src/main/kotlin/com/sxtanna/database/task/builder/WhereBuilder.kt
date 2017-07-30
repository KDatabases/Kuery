package com.sxtanna.database.task.builder

import com.sxtanna.database.struct.obj.Target
import com.sxtanna.database.type.Targeted
import com.sxtanna.database.type.base.SqlObject

abstract class WhereBuilder<T : SqlObject, W : WhereBuilder<T, W>> : Targeted<W> {

	override val where = mutableListOf<Target>()


	@JvmName("inStartsWith")
	infix fun String.startsWith(value : Any) = startsWith(this, value, false)

	infix fun String.startsNotWith(value : Any) = startsWith(this, value, true)


	@JvmName("inContains")
	infix fun String.contains(value : Any) = contains(this, value, false)

	infix fun String.containsNot(value : Any) = contains(this, value, true)


	@JvmName("inEndsWith")
	infix fun String.endWith(value : Any) = endWith(this, value, false)

	infix fun String.endNotWith(value : Any) = endWith(this, value, true)


	@JvmName("inEquals")
	infix fun String.equals(value : Any) = equals(this, value, false)

	infix fun String.equalsNot(value : Any) = equals(this, value, true)

}