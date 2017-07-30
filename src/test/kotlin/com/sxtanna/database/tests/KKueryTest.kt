package com.sxtanna.database.tests

import com.sxtanna.database.Kuery
import com.sxtanna.database.ext.co
import com.sxtanna.database.ext.sorts
import com.sxtanna.database.ext.targets
import com.sxtanna.database.ext.whileNext
import com.sxtanna.database.struct.obj.Duplicate.Update
import com.sxtanna.database.struct.obj.Sort.Order
import com.sxtanna.database.struct.obj.SqlType.VarChar
import com.sxtanna.database.struct.obj.Target.Where
import java.io.File
import kotlin.test.assertEquals

/**
 * And again for those rare Kotlin devs who love verbosity and deprecation
 */
class KKueryTest : DatabaseTest<Kuery>() {

	override fun create() = Kuery[File("../SqlConfig.json")]

	override fun runTest() {

		database {

			create("User", "name" co VarChar(255, true))

			insert("User", "name" co "Emiliee", duplicateKey = Update("name"))
			insert("User", "name" co "Sxtanna", duplicateKey = Update("name"))

			var users = 0

			select("User") {

				whileNext {
					users++
					println("Found user ${getString("name")}")
				}
			}

			assertEquals(users, 2, "Should have found 2 users, Sxtanna and Emiliee")

			select("User", targets(Where { "name" starts "S" })) {

				whileNext {
					val name = getString("name")
					println("Found user $name")

					assertEquals("Sxtanna", name, "Name should have been Sxtanna")
				}
			}

			val names = mutableListOf<String>()

			select("User", sorts(Order ascend "name")) {

				whileNext {
					val name = getString("name")
					println("Found user $name")

					names.add(name)
				}
			}

			assertEquals(names, listOf("Emiliee", "Sxtanna"), "Results were in wrong order")
		}

	}

}