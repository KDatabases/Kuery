package com.sxtanna.database.struct.base

import com.sxtanna.database.struct.obj.SqlType

data class Duo<out T>(val name : String, val value : T) {


	companion object {

		@JvmStatic
		fun <T : Any> co(name : String, value : T) = Duo(name, value)

		@JvmStatic
		@SafeVarargs
		fun valueColumns(vararg columns : Duo<Any>) = arrayOf(*columns)

		@JvmStatic
		@SafeVarargs
		fun tableColumns(vararg columns : Duo<SqlType>) = arrayOf(*columns)

	}

}