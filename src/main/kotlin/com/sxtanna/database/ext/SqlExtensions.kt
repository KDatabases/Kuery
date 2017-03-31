package com.sxtanna.database.ext

import com.sxtanna.database.struct.Column
import java.sql.ResultSet

/**
 * Special infix function for [Column]
 *
 * @param value The value to create with this Column
 * @sample createColumns
 */
infix fun <T : Any> String.co(value : T) = Column(this, value)

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


private fun createColumns() : Array<Column<Any>> {
	return Column.valueColumns("One" co 1, "Two" co 2, "True" co true)
}

private fun getUserNames(resultSet : ResultSet) {
	val names : List<String> = resultSet.mapWhileNext { getString("Username") }
	for (name in names) println("Found user $name")
}

private fun sayUserNames(resultSet : ResultSet) {
	resultSet.whileNext { println("Found user ${getString("Username")}") }
}