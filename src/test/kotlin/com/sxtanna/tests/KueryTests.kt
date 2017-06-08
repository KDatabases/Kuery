package com.sxtanna.tests

import com.sxtanna.database.Kuery
import com.sxtanna.database.ext.getBigInteger
import com.sxtanna.database.ext.getUniqueID
import com.sxtanna.database.ext.mapWhileNext
import com.sxtanna.database.struct.obj.*
import com.sxtanna.database.struct.obj.SqlType.*
import com.sxtanna.database.struct.obj.SqlType.Char
import com.sxtanna.database.task.KueryTask.InsertBuilder.Insert
import com.sxtanna.database.task.KueryTask.SelectBuilder.Select
import com.sxtanna.database.type.SqlObject
import java.io.File
import java.math.BigInteger
import java.util.*
import kotlin.system.measureTimeMillis

class KueryTests(file: File) {

	val kuery = Kuery[file]

	fun enable() = kuery.enable()

	fun disable() = kuery.disable()

	fun runTest() = kuery {
		kuery.addCreator { Test(getBigInteger("size")) }
		kuery.addCreator { Account(getUniqueID("id"), getDouble("balance"), getInt("count")) }

		createTable<Test>()

		insertTest(Test(BigInteger("10000000000000000000")))

		createTable<Account>()

		select("Account", arrayOf("balance"), arrayOf(Where.Greater("balance", 20))) {

			mapWhileNext { getDouble("balance") }.forEach { println("Found balance $it") }

		}

		selectAll {
			println("Account $id has $$balance")
		}

		select500Plus {
			println("500+ Account $id has $$balance")
		}

		createTable("Tester") {
			col("ID", Char(36, true))
			col("Name", VarChar(16))
			col("Creation", Timestamp(true))
		}

	}


	companion object {

		val insertTest = Insert.new<Test>().onDupeIgnore()

		val insertAccount = Insert.new<Account>().onDupeUpdate("id")

		val selectAll = Select.new<Account>()

		val select500Plus = Select.new<Account>().greaterThan("balance", 500.0, true).ascend("balance")

	}

}

data class Test(@PrimaryKey @Unsigned @Size(length = 200) val size : BigInteger) : SqlObject

data class Account(@PrimaryKey val id : UUID, @Size(30, 2) var balance : Double, @IntType(TinyInt::class) val count : Int) : SqlObject


fun main(vararg args : String) {
	val test = KueryTests(File("SqlConfig.json"))

	val entireTestTime = measureTimeMillis {

		val enableTime = measureTimeMillis(test::enable)
		println("Enable took $enableTime")

		val testTime = measureTimeMillis(test::runTest)
		println("Test took $testTime")

		val disableTime = measureTimeMillis(test::disable)
		println("Disable took $disableTime")

	}

	println("Entire test took $entireTestTime")
}