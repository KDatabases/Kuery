package com.sxtanna.tests

import com.sxtanna.database.Kuery
import com.sxtanna.database.ext.co
import com.sxtanna.database.struct.Duplicate
import com.sxtanna.database.struct.SqlType.Char
import java.io.File
import java.util.*

class KueryTests(file: File) {

	val kuery = Kuery[file]

	fun enable() = kuery.enable()

	fun disable() = kuery.disable()

	fun runTest() = kuery {
		createTable("Table", "ID" co Char(36))
		insert("Table", arrayOf("ID" co UUID.fromString("")), Duplicate.Update("ID"))
	}

}


fun main(vararg args : String) {
	TODO("Actually run the tests ya dink!!")
}