package com.sxtanna.database.ext

import com.sxtanna.database.struct.Duo
import com.sxtanna.database.type.JsonObject
import java.math.BigInteger
import java.sql.ResultSet
import java.util.*

/**
 * Special infix function for [Duo]
 *
 * @param value The value to create with this Duo
 * @sample createColumns
 */
infix fun <T> String.co(value : T) = Duo(this, value)


/**
 * Convenience method for reading a UUID from a ResultSet
 *
 * @param column The column name
 * @return The UUID read from the column
 */
fun ResultSet.getUniqueID(column : String) : UUID = UUID.fromString(getString(column))

/**
 * Convenience method for reading a UUID from a ResultSet
 *
 * @param column The column name
 * @return The UUID read from the column
 */
fun ResultSet.getBigInteger(column : String) : BigInteger = BigInteger(getString(column))

/**
 * Convenience method for reading Objects from a Json String
 *
 * @param column The column name
 * @return The Object read from the column
 */
inline fun <reified E : JsonObject> ResultSet.getJson(column : String) : E {
	return gson.fromJson(getString(column), E::class.java)
}


/**
 * Invoke this block for every result in this set
 *
 * @param block The action
 * @sample sayUserNames
 */
fun ResultSet.whileNext(block: ResultSet.() -> Unit) {
	while (this.next()) this.block()
}

/**
 * Use this block to map each result to an object
 *
 * @param mapper The result mapper
 * @sample getUserNames
 */
fun <O> ResultSet.mapWhileNext(mapper: ResultSet.() -> O): List<O> {
	val output = mutableListOf<O>()
	this.whileNext { output.add(this.mapper()) }

	return output
}


private fun createColumns() : Array<Duo<Any>> {
	return Duo.valueColumns("One" co 1, "Two" co 2, "True" co true)
}

private fun getUserNames(resultSet : ResultSet) {
	val names : List<String> = resultSet.mapWhileNext { getString("Username") }
	for (name in names) println("Found user $name")
}

private fun sayUserNames(resultSet : ResultSet) {
	resultSet.whileNext { println("Found user ${getString("Username")}") }
}