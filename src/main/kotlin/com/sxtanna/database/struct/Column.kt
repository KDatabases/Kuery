package com.sxtanna.database.struct

data class Column<out T : Any>(val name : String, val value : T) {


	companion object {

		@JvmStatic
		fun <T : Any> col(name : String, value : T) = Column(name, value)

		@JvmStatic
		@SafeVarargs
		fun valueColumns(vararg columns : Column<Any>) = arrayOf(*columns)

		@JvmStatic
		@SafeVarargs
		fun tableColumns(vararg columns : Column<SqlType>) = arrayOf(*columns)

	}

}