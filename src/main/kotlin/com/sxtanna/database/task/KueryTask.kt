package com.sxtanna.database.task

import com.sxtanna.database.struct.*
import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.ResultSet

class KueryTask(override val resource : Connection) : DatabaseTask<Connection>() {

	private lateinit var resultSet : ResultSet
	private lateinit var preparedStatement : PreparedStatement



	fun createTable(name : String, vararg columns : Column<SqlType>) {
		val createStatement = "Create Table If Not Exists $name ${columns.map { "${it.name} ${it.value}" }.joinToString(", ", "(", ")")}"

		preparedStatement = resource.prepareStatement(createStatement)
		preparedStatement.execute()
	}

	fun select(table : String, columns : Array<String>, where : Array<Where> = emptyArray(), preCheck: Boolean = false, handler : ResultSet.() -> Unit) {
		val selectStatement = "Select ${columns.joinToString(", ")} From $table${if (where.isNotEmpty()) " Where ${where.map(Where::toString).joinToString(" AND ")}" else ""}"

		preparedStatement = resource.prepareStatement(selectStatement)

		var offSet = 0
		where.forEachIndexed { i, where ->
			where.prepare(preparedStatement, i + 1 + offSet)
			if (where is Where.Between) offSet++
		}

		resultSet = preparedStatement.executeQuery()

		if (preCheck && resultSet.next().not()) return
		resultSet.handler()
	}

	fun insert(table : String, columns : Array<Column<Any>>, duplicateKey : Duplicate? = null) {
		val insertStatement = "Insert Into $table (${columns.map { it.name }.joinToString(", ")}) Values (${Array(columns.size, { "?" }).joinToString(", ")}) ${duplicateKey?.invoke(columns[0].name) ?: ""}"

		preparedStatement = resource.prepareStatement(insertStatement)
		columns.forEachIndexed { i, value -> Value(value.value).prepare(preparedStatement, i + 1) }

		preparedStatement.execute()
	}

	fun update(table : String, columns : Array<Column<Any>>, vararg where : Where = emptyArray()) {
		val updateStatement = "Update $table Set ${columns.map { "${it.name} = ?" }.joinToString(", ")}${if (where.isNotEmpty()) " Where ${where.map(Where::toString).joinToString(" AND ")}" else ""}"

		preparedStatement =  resource.prepareStatement(updateStatement)
		columns.forEachIndexed { i, value -> Value(value.value).prepare(preparedStatement, i + 1) }

		var offSet = columns.size
		where.forEachIndexed { i, where ->
			where.prepare(preparedStatement, i + 1 + offSet)
			if (where is Where.Between) offSet++
		}

		preparedStatement.executeUpdate()
	}

}