package com.sxtanna.database.task

import com.sxtanna.database.Kuery
import com.sxtanna.database.ext.*
import com.sxtanna.database.struct.base.Duo
import com.sxtanna.database.struct.obj.Duplicate
import com.sxtanna.database.struct.obj.Duplicate.Ignore
import com.sxtanna.database.struct.obj.Duplicate.Update
import com.sxtanna.database.struct.obj.Sort
import com.sxtanna.database.struct.obj.SqlType
import com.sxtanna.database.struct.obj.Target
import com.sxtanna.database.task.builder.CreateBuilder
import com.sxtanna.database.task.builder.InsertBuilder
import com.sxtanna.database.task.builder.SelectBuilder
import com.sxtanna.database.task.builder.UpdateBuilder
import com.sxtanna.database.type.base.SqlObject
import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.util.function.Consumer
import kotlin.reflect.KProperty1
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.jvm.isAccessible

@Suppress("UNCHECKED_CAST")
class KueryTask(private val kuery : Kuery, override val resource : Connection) : DatabaseTask<Connection>() {

	private lateinit var resultSet : ResultSet
	private lateinit var preparedStatement : PreparedStatement


	@JvmSynthetic
	fun query(query : String, vararg values : Any?, block : ResultSet.() -> Unit) {
		if (kuery.debug) println("Query '$query'")

		preparedStatement = resource.prepareStatement(query)
		values.forEachIndexed { index, any -> preparedStatement.setObject(index + 1, any) }

		resultSet = preparedStatement.executeQuery()
		resultSet.block()
	}

	@JvmOverloads
	fun query(query : String, vararg values : Any? = emptyArray(), block : Consumer<ResultSet>) = query(query, *values) { block.accept(this) }


	fun execute(query : String, vararg values : Any?) {
		if (kuery.debug) println("Statement '$query'")

		preparedStatement = resource.prepareStatement(query)
		if (values.isNotEmpty()) values.forEachIndexed { index, any -> preparedStatement.setObject(index + 1, any) }

		preparedStatement.execute()
	}


	@SafeVarargs
	@Deprecated("Use CreateBuilder", ReplaceWith("Create.table(name).cos(*columns)", "com.sxtanna.database.task.builder.CreateBuilder.Create"))
	fun create(name : String, vararg columns : Duo<SqlType>) {
		execute("CREATE TABLE IF NOT EXISTS $name ${columns.map { "${it.name} ${it.value}" }.joinToString(prefix = "(", postfix = ")")}")
	}


	@JvmSynthetic
	@Deprecated("Use SelectBuilder")
	fun select(table : String, columns : Array<out String> = ALL_ROWS, target : Array<out Target> = NO_WHERE, order : Array<out Sort> = NO_SORTS, block : ResultSet.() -> Unit) {
		val statement = "SELECT ${columns.joinToString()} FROM $table${target.isNotEmpty().value(" WHERE ${target.joinToString(" AND ")}")}${order.isNotEmpty().value(" ORDER BY ${order.joinToString()}")}"

		if (kuery.debug) println("Select '$statement'")

		preparedStatement = resource.prepareStatement(statement)

		var offSet = 0
		target.forEachIndexed { i, data -> offSet += data.prep(preparedStatement, i + 1 + offSet) }

		resultSet = preparedStatement.executeQuery()
		if (resultSet.isBeforeFirst.not()) return resultSet.close()

		block(resultSet)
	}

	@JvmOverloads
	@Deprecated("Use SelectBuilder")
	fun select(table : String, columns : Array<out String> = ALL_ROWS, target : Array<out Target> = NO_WHERE, order : Array<out Sort> = NO_SORTS, block : Consumer<ResultSet>) {
		select(table, columns, target, order) { block.accept(this) }
	}

	@JvmSynthetic
	@Deprecated("Use SelectBuilder")
	fun select(table : String, order : Array<out Sort>, block : ResultSet.() -> Unit) = select(table, ALL_ROWS, NO_WHERE, order, block)

	@JvmSynthetic
	@Deprecated("Use SelectBuilder")
	fun select(table : String, target : Array<out Target>, block : ResultSet.() -> Unit) = select(table, ALL_ROWS, target, NO_SORTS, block)

	@Deprecated("Use SelectBuilder")
	fun select(table : String, order : Array<out Sort>, block : Consumer<ResultSet>) = select(table, ALL_ROWS, NO_WHERE, order, block)

	@Deprecated("Use SelectBuilder")
	fun select(table : String, target : Array<out Target>, block : Consumer<ResultSet>) = select(table, ALL_ROWS, target, NO_SORTS, block)


	@JvmOverloads
	@Deprecated("Use InsertBuilder")
	fun insert(table : String, vararg columns : Duo<Any?>, duplicateKey : Duplicate? = null) {
		val ignore = duplicateKey is Ignore
		val insertStatement = "INSERT ${ignore.value("IGNORE ")}INTO $table (${columns.map { it.name }.joinToString()}) VALUES ${values(columns.size)}${ignore.not().value(" ${duplicateKey?.invoke(columns[0].name)}")}"

		execute(insertStatement, *columns.map { it.value }.toTypedArray())
	}


	@JvmOverloads
	@Deprecated("Use UpdateBuilder")
	fun update(table : String, columns : Array<Duo<Any?>>, vararg where : Target = emptyArray()) : Int {
		val updateStatement = "UPDATE $table SET ${columns.map { "${it.name}=?" }.joinToString()}${where.isNotEmpty().value(" WHERE ${where.joinToString(" AND ")}")}"

		if (kuery.debug) println("Update '$updateStatement'")

		preparedStatement = resource.prepareStatement(updateStatement)
		columns.forEachIndexed { i, data -> preparedStatement.setObject(i + 1, data.value) }

		var offSet = columns.size
		where.forEachIndexed { i, data -> offSet += data.prep(preparedStatement, i + 1 + offSet) }

		return preparedStatement.executeUpdate()
	}


	//region Building Functions
	@JvmSynthetic
	operator fun CreateBuilder.invoke() = create(table, *columns.toTypedArray())

	fun execute(create : CreateBuilder) = create.invoke()


	@JvmSynthetic
	operator fun <T : SqlObject> SelectBuilder<T>.invoke(handler : T.() -> Unit) {
		select(table, ALL_ROWS, where.toTypedArray(), sorts.toTypedArray()) {
			val creator = checkNotNull(kuery.creators[clazz] as? ResultSet.() -> T) { "Creator for type $clazz doesn't exist" }
			mapWhileNext(creator).forEach(handler)
		}
	}

	fun <T : SqlObject> execute(select : SelectBuilder<T>, handler : Consumer<T>) = select.invoke { handler.accept(this) }


	@JvmSynthetic
	operator fun <T : SqlObject> InsertBuilder<T>.invoke(obj : T) {
		val columns = clazz.declaredMemberProperties.map { it.name co obj.retrieve(it) }.toTypedArray()
		insert(table, *columns, duplicateKey = if (ignore) Ignore() else if (update.isNotEmpty()) Update(*update) else null)
	}

	fun <T : SqlObject> execute(insert : InsertBuilder<T>, obj : T) = insert.invoke(obj)


	@JvmSynthetic
	operator fun <T : SqlObject> UpdateBuilder<T>.invoke(obj : T) : Int {
		val columns = clazz.declaredMemberProperties.filterNot { it.name.toLowerCase() in ignoring }.map { it.name co obj.retrieve(it) }.toTypedArray()
		return update(table, columns, *where.toTypedArray())
	}

	fun <T : SqlObject> execute(update : UpdateBuilder<T>, obj : T) = update.invoke(obj)
	//endregion


	private fun values(count : Int) = Array(count, { "?" }).joinToString(prefix = "(", postfix = ")")

	private fun <T, R> T.retrieve(property : KProperty1<T, R>) : R {
		val state = property.isAccessible

		property.isAccessible = true
		val obj = property.get(this)
		property.isAccessible = state

		return obj
	}

}