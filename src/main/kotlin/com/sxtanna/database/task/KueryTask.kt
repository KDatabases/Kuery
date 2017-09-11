package com.sxtanna.database.task

import com.sxtanna.database.Kuery
import com.sxtanna.database.ext.*
import com.sxtanna.database.struct.base.Duo
import com.sxtanna.database.struct.obj.Duplicate
import com.sxtanna.database.struct.obj.Duplicate.Ignore
import com.sxtanna.database.struct.obj.Sort
import com.sxtanna.database.struct.obj.SqlType
import com.sxtanna.database.struct.obj.Target
import com.sxtanna.database.task.builder.*
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
		if (kuery.debug) kuery.logger.info("Query '$query'")

		preparedStatement = resource.prepareStatement(query)
		values.forEachIndexed { index, any -> preparedStatement.setObject(index + 1, any) }

		resultSet = preparedStatement.executeQuery()
		resultSet.block()
	}

	@JvmOverloads
	fun query(query : String, vararg values : Any? = emptyArray(), block : Consumer<ResultSet>) = query(query, *values) { block.accept(this) }


	fun execute(query : String, vararg values : Any?) {
		if (kuery.debug) kuery.logger.info("Statement '$query'")

		preparedStatement = resource.prepareStatement(query)
		if (values.isNotEmpty()) values.forEachIndexed { index, any -> preparedStatement.setObject(index + 1, any) }

		preparedStatement.execute()
	}


	@SafeVarargs
	@Deprecated("Use Create", ReplaceWith("Create.table(name).cos(*columns)", "com.sxtanna.database.task.builder.Create"))
	fun create(name : String, vararg columns : Duo<SqlType>) {
		execute("CREATE TABLE IF NOT EXISTS $name ${columns.map { "${it.name} ${it.value}" }.joinToString(prefix = "(", postfix = ")")}")
	}


	@JvmSynthetic
	@Deprecated("Use Select")
	fun select(table : String, columns : Array<out String> = ALL_ROWS, target : Array<out Target> = NO_WHERE, order : Array<out Sort> = NO_SORTS, block : ResultSet.() -> Unit) {
		val statement = "SELECT ${columns.joinToString()} FROM $table${target.isNotEmpty().value(" WHERE ${target.joinToString(" AND ")}")}${order.isNotEmpty().value(" ORDER BY ${order.joinToString()}")}"

		if (kuery.debug) kuery.logger.info("Select '$statement'")

		preparedStatement = resource.prepareStatement(statement)

		var offSet = 0
		target.forEachIndexed { i, data -> offSet += data.prep(preparedStatement, i + 1 + offSet) }

		resultSet = preparedStatement.executeQuery()
		if (resultSet.isBeforeFirst.not()) return resultSet.close()

		block(resultSet)
	}

	@JvmOverloads
	@Deprecated("Use Select")
	fun select(table : String, columns : Array<out String> = ALL_ROWS, target : Array<out Target> = NO_WHERE, order : Array<out Sort> = NO_SORTS, block : Consumer<ResultSet>) {
		select(table, columns, target, order) { block.accept(this) }
	}

	@JvmSynthetic
	@Deprecated("Use Select")
	fun select(table : String, order : Array<out Sort>, block : ResultSet.() -> Unit) = select(table, ALL_ROWS, NO_WHERE, order, block)

	@JvmSynthetic
	@Deprecated("Use Select")
	fun select(table : String, target : Array<out Target>, block : ResultSet.() -> Unit) = select(table, ALL_ROWS, target, NO_SORTS, block)

	@Deprecated("Use Select")
	fun select(table : String, order : Array<out Sort>, block : Consumer<ResultSet>) = select(table, ALL_ROWS, NO_WHERE, order, block)

	@Deprecated("Use Select")
	fun select(table : String, target : Array<out Target>, block : Consumer<ResultSet>) = select(table, ALL_ROWS, target, NO_SORTS, block)


	@JvmOverloads
	@Deprecated("Use Insert")
	fun insert(table : String, vararg columns : Duo<Any?>, duplicateKey : Duplicate? = null) {
		val ignore = duplicateKey is Ignore
		val insertStatement = "INSERT ${ignore.value("IGNORE ")}INTO $table (${columns.map { it.name }.joinToString()}) VALUES ${values(columns.size)}${ignore.not().value(" ${duplicateKey?.invoke(columns[0].name)}")}"

		execute(insertStatement, *columns.map { it.value }.toTypedArray())
	}


	@JvmOverloads
	@Deprecated("Use Update")
	fun update(table : String, columns : Array<Duo<Any?>>, vararg where : Target = emptyArray()) : Int {
		val updateStatement = "UPDATE $table SET ${columns.joinToString { "${it.name}=?" }}${where.isNotEmpty().value(" WHERE ${where.joinToString(" AND ")}")}"

		if (kuery.debug) kuery.logger.info("Update '$updateStatement'")

		preparedStatement = resource.prepareStatement(updateStatement)
		columns.forEachIndexed { i, data -> preparedStatement.setObject(i + 1, data.value) }

		var offSet = columns.size
		where.forEachIndexed { i, data -> offSet += data.prep(preparedStatement, i + 1 + offSet) }

		return preparedStatement.executeUpdate()
	}


    @JvmOverloads
    @Deprecated("Use Delete")
    fun delete(table : String, vararg where : Target = emptyArray()) {
        val deleteStatement = "DELETE FROM $table ${where.isNotEmpty().value("WHERE ${where.joinToString(" AND ")}")}"

        if (kuery.debug) kuery.logger.info("Delete '$deleteStatement'")

        preparedStatement = resource.prepareStatement(deleteStatement)
        where.forEachIndexed { i, data -> data.prep(preparedStatement, i + 1) }

        preparedStatement.execute()
    }


	//region Building Functions
	@JvmSynthetic
	operator fun Create.invoke() = create(table, *columns.toTypedArray())

	fun execute(create : Create) = create.invoke()


	@JvmSynthetic
	operator fun <T : SqlObject> Select<T>.invoke(handler : T.() -> Unit) {
		select(table, ALL_ROWS, where.toTypedArray(), sorts.toTypedArray()) {
			val creator = checkNotNull(kuery.creators[clazz] as? ResultSet.() -> T) { "Creator for type $clazz doesn't exist" }
			mapWhileNext(creator).forEach(handler)
		}
	}

	fun <T : SqlObject> execute(select : Select<T>, handler : Consumer<T>) = select.invoke { handler.accept(this) }


	@JvmSynthetic
	operator fun <T : SqlObject> Insert<T>.invoke(obj : T) {
		val columns = clazz.declaredMemberProperties.map { it.name co obj.retrieve(it) }.toTypedArray()
		insert(table, *columns, duplicateKey = if (ignore) Ignore() else if (update.isNotEmpty()) Duplicate.Update(*update) else null)
	}

	fun <T : SqlObject> execute(insert : Insert<T>, obj : T) = insert.invoke(obj)


	@JvmSynthetic
	operator fun <T : SqlObject> Update<T>.invoke(obj : T) : Int {
		val columns = clazz.declaredMemberProperties.filterNot { it.name.toLowerCase() in ignoring }.map { it.name co obj.retrieve(it) }.toTypedArray()
		return update(table, columns, *where.toTypedArray())
	}

	fun <T : SqlObject> execute(update : Update<T>, obj : T) = update.invoke(obj)

    @JvmSynthetic
    operator fun <T : SqlObject> Delete<T>.invoke(obj : T? = null) {
        val where = obj?.let { o -> clazz.declaredMemberProperties.map { Target.equals(it.name, o.retrieve(it)) } } ?: where
        return delete(table, *where.toTypedArray())
    }

    /**
     * If an object is supplied, it will use the data from that object,
     * if not, it will fallback to the clauses specified in the statement
     * If no conditions are supplied, it will delete every row
     */
    @JvmOverloads
    fun <T : SqlObject> execute(delete : Delete<T>, obj : T? = null) = delete.invoke(obj)
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