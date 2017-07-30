package com.sxtanna.database.type

import com.sxtanna.database.struct.obj.Sort
import com.sxtanna.database.struct.obj.Sort.Direction
import com.sxtanna.database.struct.obj.Sort.Direction.ASCEND
import com.sxtanna.database.struct.obj.Sort.Direction.DESCEND
import com.sxtanna.database.struct.obj.Sort.Order

@Suppress("UNCHECKED_CAST")
interface Sorted<T : Sorted<T>> {

	val sorts : MutableList<Sort>


	fun by(vararg columns : Pair<String, Direction>) : T {
		columns.forEach { sorts.add(if (it.second == ASCEND) Order.ascend(it.first) else Order.descend(it.first)) }
		return this as T
	}

	fun ascend(column : String) = by(column to ASCEND)

	fun descend(column : String) = by(column to DESCEND)

}