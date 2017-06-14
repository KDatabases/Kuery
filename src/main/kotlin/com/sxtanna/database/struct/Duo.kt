package com.sxtanna.database.struct

import com.sxtanna.database.struct.obj.SqlType

data class Duo<out T>(val name : String, val value : T) {


	companion object {

		@JvmStatic
		@SafeVarargs
		fun valueColumns(vararg columns : Duo<Any>) = arrayOf(*columns)

		@JvmStatic
		@SafeVarargs
		fun tableColumns(vararg columns : Duo<SqlType>) = arrayOf(*columns)

	}

}