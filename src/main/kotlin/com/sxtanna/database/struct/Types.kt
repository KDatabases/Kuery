package com.sxtanna.database.struct

import com.sxtanna.database.ext.*
import com.sxtanna.database.struct.Sort.Type.Ascending
import com.sxtanna.database.struct.Sort.Type.Descending
import java.sql.PreparedStatement
import kotlin.reflect.KClass

sealed class SqlType(val name : String) {

	open val primaryKey : Boolean = false
	open val notNull : Boolean = true


	protected open fun name() = name


	override fun toString() : String = "${name()}${if (primaryKey) " Primary Key" else ""}${if (notNull) " Not Null" else ""}"


	//region Base Types
	abstract class SizedSqlType(val size : Long, name : String) : SqlType(name) {

		override fun name() : String = "$name(${size()})"

		open fun size() = size

		override fun toString() : String {
			val outputName = name()
			return "$outputName${super.toString().substringAfter(outputName)}"
		}

	}

	abstract class NumberSqlType(size : Long, name : String) : SizedSqlType(size, name) {

		open val unsigned : Boolean = false


		open fun numberName() = super.name()

		override fun size() : Long = super.size().coerceIn(range())

		override fun name() : String = "${numberName()}${if (unsigned) " UNSIGNED" else ""}"


		abstract fun range() : LongRange

	}

	abstract class WholeNumberSqlType(size : Long, name : String) : NumberSqlType(size, name)

	abstract class DecimalSqlType(fullSize : Int, val decimals : Int, name : String) : NumberSqlType(fullSize.toLong(), name) {

		override fun numberName() : String = "$name(${size()}, $decimals)"

		override fun range() : LongRange = 0L..30L

	}
	//endregion


	//region Number Types
	class TinyInt @JvmOverloads constructor(size : Int, override val unsigned : Boolean = false, override val primaryKey : Boolean = false, override val notNull : Boolean = true) : WholeNumberSqlType(size.toLong(), "TINYINT") {

		override fun range() : LongRange = if (unsigned) MIN_UNSIGN..TINY_MAX_UNSIGN else TINY_MIN_SIGN..TINY_MAX_SIGN

	}

	class SmallInt @JvmOverloads constructor(size : Int, override val unsigned : Boolean = false, override val primaryKey : Boolean = false, override val notNull : Boolean = true) : WholeNumberSqlType(size.toLong(), "SMALLINT") {

		override fun range() : LongRange = if (unsigned) MIN_UNSIGN..SMALL_MAX_UNSIGN else SMALL_MIN_SIGN..SMALL_MAX_SIGN

	}

	class MediumInt @JvmOverloads constructor(size : Int, override val unsigned : Boolean = false, override val primaryKey : Boolean = false, override val notNull : Boolean = true) : WholeNumberSqlType(size.toLong(), "MEDIUMINT") {

		override fun range() : LongRange = if (unsigned) MIN_UNSIGN..MEDIUM_MAX_UNSIGN else MEDIUM_MIN_SIGN..MEDIUM_MAX_SIGN

	}

	class NormInt @JvmOverloads constructor(size : Long, override val unsigned : Boolean = false, override val primaryKey : Boolean = false, override val notNull : Boolean = true) : WholeNumberSqlType(size, "INT") {

		override fun range() : LongRange = if (unsigned) MIN_UNSIGN..NORM_MAX_UNSIGN else NORM_MIN_SIGN..NORM_MAX_SIGN

	}

	class Decimal @JvmOverloads constructor(size : Int = 10, decimals : Int = 0, override val primaryKey : Boolean = false, override val notNull : Boolean = true) : DecimalSqlType(size, decimals, "DECIMAL")
	//endregion


	//region Character Types
	class Char @JvmOverloads constructor(size : Int, override val primaryKey : Boolean = false, override val notNull : Boolean = true) : SizedSqlType(size.toLong(), "CHAR")

	class VarChar @JvmOverloads constructor(size : Int, override val primaryKey : Boolean = false, override val notNull : Boolean = true) : SizedSqlType(size.toLong(), "VARCHAR")
	//endregion


	//region Misc Types
	class Bool @JvmOverloads constructor(override val primaryKey : Boolean = false, override val notNull : Boolean = true) : SqlType("BOOLEAN")

	class EnumSet @JvmOverloads constructor(val enumClass : KClass<out Enum<*>>, override val primaryKey : Boolean = false, override val notNull : Boolean = true) : SqlType("ENUM") {

		override fun toString() : String {
			val constants = enumClass.java.enumConstants.joinToString(", ") { "'$it'" }
			return "$name($constants)${super.toString().substringAfter(name)}"
		}

	}

	class ValueSet @JvmOverloads constructor(val values : Array<String>, override val primaryKey : Boolean = false, override val notNull : Boolean = true) : SqlType("SET") {

		override fun toString() : String {
			val clampedValues = Array(64) { values[it] }.joinToString(", ") { "'$it'" }
			return "$name($clampedValues)${super.toString().substringAfter(name)}"
		}

	}

	class Timestamp @JvmOverloads constructor(val current : Boolean = false, val updating : Boolean = false, override val primaryKey : Boolean = false, override val notNull : Boolean = true) : SqlType("TIMESTAMP") {

		override fun toString() : String {
			var base = super.toString()
			if (current) base += CURRENT
			if (updating) base += "${if (current.not()) CURRENT else ""}$UPDATE"

			return base
		}


		companion object {

			const val UPDATE = " ON UPDATE CURRENT_TIMESTAMP"
			const val CURRENT = " DEFAULT CURRENT_TIMESTAMP"

		}

	}
	//endregion

}

sealed class Duplicate {

	abstract fun invoke(key : String) : String


	class Ignore : Duplicate() {

		override operator fun invoke(key : String) : String = "On Duplicate Key Update $key=$key"

	}

	class Update(vararg val value : String) : Duplicate() {

		override fun invoke(key : String) : String = "On Duplicate Key Update ${value.map { "$it=Values($it)" }.joinToString(", ")}"

	}

}

sealed class Where(val column : String, val data : Any) {

	open fun prepare(prep : PreparedStatement, position : Int) {
		set(prep, position, data)
	}

	protected fun set(prep : PreparedStatement, position : Int, data : Any) {
		when (data) {
			is Byte -> prep.setByte(position, data)
			is Short -> prep.setShort(position, data)
			is Int -> prep.setInt(position, data)
			is Long -> prep.setLong(position, data)
			is Double -> prep.setDouble(position, data)
			is String -> prep.setString(position, data)
			else -> prep.setString(position, data.toString())
		}
	}


	open class RelationalWhere(column : String, data : Any, val orEqual : Boolean, val symbol : Char) : Where(column, data) {

		override fun toString() : String = "$column $symbol${if (orEqual) "=" else ""} ?"

	}


	class Equals(column : String, data : Any) : Where(column, data) {

		override fun toString() : String = "$column=?"

	}

	class Like(column : String, data : Any, val option : LikeOption, val not : Boolean = false) : Where(column, data) {

		override fun toString() : String = "$column ${if (not) "NOT " else ""}LIKE ${option.block}"


		enum class LikeOption(val block : String) {

			Starts("?$WILDCARD"),
			Ends("$WILDCARD?"),
			Contains("$WILDCARD?$WILDCARD");

		}

		companion object {

			const val WILDCARD = '%'

		}

	}

	class Between(column : String, first : Any, val second : Any, val not : Boolean = false) : Where(column, first) {

		override fun prepare(prep : PreparedStatement, position : Int) {
			set(prep, position, data)
			set(prep, position + 1, second)
		}

		override fun toString() : String = "$column ${if (not) "NOT " else ""}BETWEEN ? AND ?"

	}

	class Greater(column : String, data : Any, orEqual : Boolean = false) : RelationalWhere(column, data, orEqual, '>')

	class Less(column : String, data : Any, orEqual : Boolean = false) : RelationalWhere(column, data, orEqual, '<')

}

sealed class Sort(val column : String, val type : Type) {

	override fun toString() : String = "$column $type"


	class Ascend(column : String) : Sort(column, Ascending)

	class Descend(column : String) : Sort(column, Descending)


	enum class Type(val value : String) {

		Ascending("ASC"),
		Descending("DESC");

		override fun toString() : String = value
	}

}