package com.sxtanna.database.struct.obj

import com.sxtanna.database.struct.obj.Sort.Direction.ASCEND
import com.sxtanna.database.struct.obj.Sort.Direction.DESCEND

sealed class Sort(private val column : String, private val type : Direction) {

	override fun toString() = "$column $type"


	private class Ascend(column : String) : Sort(column, ASCEND)

	private class Descend(column : String) : Sort(column, DESCEND)


	enum class Direction(private val value : String) {

		ASCEND  ("ASC"),
		DESCEND ("DESC");

		override fun toString() = value
	}


	companion object Order {

		@JvmSynthetic
		operator fun <R> invoke(block : Order.() -> R) = this.block()

		operator fun get(vararg target : Target) = target


		@JvmStatic
		infix fun ascend(name : String) : Sort = Ascend(name)

		@JvmStatic
		infix fun descend(name : String) : Sort = Descend(name)

	}

}