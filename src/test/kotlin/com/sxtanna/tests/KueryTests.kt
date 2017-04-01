package com.sxtanna.tests

import com.sxtanna.database.Kuery
import com.sxtanna.database.ext.getUniqueID
import com.sxtanna.database.struct.obj.base.PrimaryKey
import com.sxtanna.database.struct.obj.base.Size
import com.sxtanna.database.task.KueryTask.InsertBuilder.Insert
import com.sxtanna.database.task.KueryTask.SelectBuilder.Select
import com.sxtanna.database.type.SqlObject
import java.io.File
import java.util.*
import java.util.concurrent.ThreadLocalRandom
import kotlin.system.measureTimeMillis

class KueryTests(file: File) {

	val kuery = Kuery[file]

	fun enable() = kuery.enable()

	fun disable() = kuery.disable()

	fun runTest() = kuery {
		kuery.addCreator { Account(getUniqueID("id"), getDouble("balance")) }
		createTable<Account>()

		for (i in 1..100) insertAccount(Account(UUID.randomUUID(), ThreadLocalRandom.current().nextDouble(0.0, 1000.0)))
		selectAll { println("Account $id has $$balance") }

		select500Plus {
			println("Account $id has $$balance")
		}
	}


	companion object {

		val insertAccount = Insert.new<Account>().onDupeUpdate("id")
		val selectAll = Select.new<Account>()
		val select500Plus = Select.new<Account>().greaterThan("balance", 500.0, true).ascend("balance")

	}

}


data class Account(@PrimaryKey val id : UUID, @Size(30, 2) var balance : Double) : SqlObject


fun main(vararg args : String) {
	val test = KueryTests(File("SqlConfig.json"))

	val entireTestTime = measureTimeMillis {
		println("Enable took ${measureTimeMillis {
			test.enable()
		}}")

		println("Test took ${measureTimeMillis {
			test.runTest()
		}}")

		println("Disable took ${measureTimeMillis {
			test.disable()
		}}")
	}

	println("Entire test took $entireTestTime")
}