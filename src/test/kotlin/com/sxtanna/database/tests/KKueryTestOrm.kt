package com.sxtanna.database.tests

import com.sxtanna.database.Kuery
import com.sxtanna.database.ext.PrimaryKey
import com.sxtanna.database.task.builder.CreateBuilder.Create
import com.sxtanna.database.task.builder.InsertBuilder
import com.sxtanna.database.task.builder.InsertBuilder.Insert
import com.sxtanna.database.task.builder.SelectBuilder.Select
import com.sxtanna.database.type.base.SqlObject
import java.io.File
import kotlin.test.assertEquals

class KKueryTestOrm : DatabaseTest<Kuery>() {

	override fun create() = Kuery[File("../SqlConfig.json")]

	override fun runTest() {
		database.addCreator { User(getString("name")) }

		database {

			// You can either use the #invoke() function or use a direct call
			createUser()

			// You can either use the #invoke(obj : T) function or use a direct call
			insertUser(User("Emiliee"))
			insertUser(User("Sxtanna"))

			var users = 0

			// Select blocks will run for each returned object, in it's scope
			selectUsers {
				users++

				// Since we are in the scope of the object,
				// we have direct access to its properties, ex. "name"

				println("Found user $name")
			}

			assertEquals(2, users, "Should have found 2 users, Sxtanna and Emiliee")

			selectSNames {

				assertEquals("Sxtanna", name, "Name should have been Sxtanna")
				println("Found user $name")

			}

			val names = mutableListOf<String>()

			selectAscendNames {

				println("Found user $name")
				names.add(name)

			}

			assertEquals(names, listOf("Emiliee", "Sxtanna"), "Results were in wrong order")

		}
	}


	companion object {

		/**
		 * Simple create statement, will gather information from the class
		 */
		val createUser = Create.from<User>()

		/**
		 * Simple insert statement, will update primary key if duplicate.
		 *
		 * [InsertBuilder.onDupeUpdate] accepts specific columns.
		 */
		val insertUser = Insert.into<User>().onDupeUpdate()

		/**
		 * Simple select statement, will select all rows
		 */
		val selectUsers = Select.from<User>()

		/**
		 * This statement will select all users whose name starts with the letter 'S'
		 */
		val selectSNames = Select.from<User>().startsWith("name", "S")

		/**
		 * This statement will select all users and order them by name
		 */
		val selectAscendNames = Select.from<User>().ascend("name")


		/**
		 * This will outline the database table, each field will get its own row.
		 *
		 * A [PrimaryKey] annotation will denote which field is the table's primary key
		 */
		data class User(@PrimaryKey val name : String) : SqlObject

	}

}