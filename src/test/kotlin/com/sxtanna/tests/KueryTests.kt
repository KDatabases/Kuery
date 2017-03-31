package com.sxtanna.tests

import com.sxtanna.database.Kuery
import com.sxtanna.database.ext.co
import com.sxtanna.database.struct.SqlType.Char
import com.sxtanna.database.struct.SqlType.VarChar
import java.io.File

class KueryTests(file: File) {

	val kuery = Kuery[file]

	fun enable() = kuery.enable()

	fun disable() = kuery.disable()

	fun runTest() = kuery {
		createTable("Table", "UUID" co Char(36, true), "Name" co VarChar(36))
	}

}


fun main(vararg args : String) {
	TODO("Actually run the tests ya dink!!")
}