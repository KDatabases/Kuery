@file:JvmName("Krs")

package com.sxtanna.database.ext

import java.math.BigInteger
import java.sql.ResultSet
import java.util.*
import java.util.UUID.fromString
import java.util.function.Consumer
import java.util.function.Function

/**
 * Read a [UUID] from a [ResultSet]
 *
 * @param column The column name
 * @return The UUID read from the column
 */
@JvmSynthetic
@JvmName("gUUID")
fun ResultSet.getUUID(column : String) = getUUID(this, column)

/**
 * Read a [BigInteger] from a [ResultSet]
 *
 * @param column The column name
 * @return The UUID read from the column
 */
@JvmSynthetic
@JvmName("gBINT")
fun ResultSet.getBigInt(column : String) = getBigInt(this, column)

/**
 * Read any object [J] from the [ResultSet]
 *
 * **Row data must actually contain serialized object**
 */
@JvmSynthetic
inline fun <reified J : Any> ResultSet.getJson(column : String) = getJson(this, J::class.java, column)

/**
 * Read an Enum constant from a [ResultSet]
 */
@JvmSynthetic
inline fun <reified E : Enum<E>> ResultSet.getEnum(column : String) = getEnum(this, E::class.java, column)


/**
 * Execute this block of code for every result in the set
 * @sample sayUserNames
 */
@JvmSynthetic
inline fun ResultSet.whileNext(block : ResultSet.() -> Unit) {
	while (this.next()) this.block()
}

/**
 * Map each result in the set to an object and return a list of them
 * @sample getUserNames
 */
@JvmSynthetic
inline fun <O> ResultSet.mapWhileNext(mapper : ResultSet.() -> O) : List<O> {
	val output = mutableListOf<O>()
	this.whileNext { output.add(this.mapper()) }

	return output
}


/**
 * Convenience methods for accessing ResultSet data without having to worry about catching exceptions
 */

fun getInt(rs : ResultSet, column : String) = attempt { rs.getInt(column) }

fun getLong(rs : ResultSet, column : String) = attempt { rs.getLong(column) }

fun getBoolean(rs : ResultSet, column : String) = attempt { rs.getBoolean(column) }

fun getString(rs : ResultSet, column : String) = attempt { rs.getString(column) }

fun getDouble(rs : ResultSet, column : String) = attempt { rs.getDouble(column) }

fun getUUID(rs : ResultSet, column : String) : UUID {
	val uuid = rs.getString(column)
	return checkNotNull(fromString(uuid)) { "Bad UUID $uuid at column $column" }
}

fun getBigInt(rs : ResultSet, column : String) = BigInteger(rs.getString(column))

fun <J : Any> getJson(rs : ResultSet, clazz : Class<J>, column : String) : J {
	val data = rs.getString(column)
	return checkNotNull(attempt { gson.fromJson(data, clazz) }) { "Json data $data at column $column failed to be deserialized" }
}

fun <E : Enum<E>> getEnum(rs : ResultSet, clazz : Class<E>, column : String) : E {
	val name = rs.getString(column)
	return checkNotNull(clazz.enumConstants.find { it.name == name }) { "Enum value $name at column $column doesn't exist" }
}


fun whileNext(rs : ResultSet, block : Consumer<ResultSet>) = rs.whileNext { block.accept(this) }

fun <O : Any> mapWhileNext(rs : ResultSet, block : Function<ResultSet, O>) = rs.mapWhileNext { block.apply(this) }


private fun getUserNames(resultSet : ResultSet) {
	val names = resultSet.mapWhileNext { getString("Username") }
	for (name in names) println("Found user $name")
}

private fun sayUserNames(resultSet : ResultSet) {
	resultSet.whileNext { println("Found user ${getString("Username")}") }
}