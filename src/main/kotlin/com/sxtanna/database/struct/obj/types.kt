package com.sxtanna.database.struct.obj

import com.sxtanna.database.struct.obj.Sort.Type.Ascending
import com.sxtanna.database.struct.obj.Sort.Type.Descending
import java.math.BigInteger
import java.sql.PreparedStatement
import kotlin.reflect.KClass

sealed class SqlType(val name : String) {

	open var primaryKey : Boolean = false
	open var notNull : Boolean = true


	protected open fun name() = name

	override fun toString() : String = "${name()}${if (primaryKey) " Primary Key" else ""}${if (notNull) " Not Null" else ""}"


	//region Base Types
	abstract class SizedSqlType<N>(val size : N, name : String) : SqlType(name) where N : Number, N : Comparable<N> {

		override fun name() : String = "$name(${size()})"

		open fun size() = size

		override fun toString() : String {
			val outputName = name()
			return "$outputName${super.toString().substringAfter(outputName)}"
		}

	}

	abstract class NumberSqlType<N>(size : N, name : String) : SizedSqlType<N>(size, name) where N : Number, N : Comparable<N> {

		open val unsigned : Boolean = false


		open fun numberName() = super.name()

		override fun name() : String = "${numberName()}${if (unsigned) " UNSIGNED" else ""}"

	}

	abstract class WholeNumberSqlType<N>(size : N, name : String) : NumberSqlType<N>(size, name) where N : Number, N : Comparable<N>

	abstract class DecimalSqlType(fullSize : Long, val decimals : Int, name : String) : NumberSqlType<Long>(fullSize, name) {

		override fun numberName() : String = "$name(${size()}, $decimals)"

	}
	//endregion

	//region Number Types
	/**
	 * Has the same range as an [Int]
	 *
	 * - **Signed Range** -2,147,483,648 to 2,147,483,647
	 * - **Unsigned Range** 0 to 4,294,967,295
	 */
	class NormInt @JvmOverloads constructor(size : Long, override val unsigned : Boolean = false, override var primaryKey : Boolean = false, override var notNull : Boolean = true) : WholeNumberSqlType<Long>(size, "INT")

	/**
	 * Has the same range as a [Byte]
	 *
	 * - **Signed Range** -128 to 127
	 * - **Unsigned Range** 0 to 255
	 */
	class TinyInt @JvmOverloads constructor(size : Int, override val unsigned : Boolean = false, override var primaryKey : Boolean = false, override var notNull : Boolean = true) : WholeNumberSqlType<Long>(size.toLong(), "TINYINT")

	/**
	 * Has the same range as a [Short]
	 *
	 * - **Signed Range** -32,768 to 32,767
	 * - **Unsigned Range** 0 to 65,535
	 */
	class SmallInt @JvmOverloads constructor(size : Int, override val unsigned : Boolean = false, override var primaryKey : Boolean = false, override var notNull : Boolean = true) : WholeNumberSqlType<Long>(size.toLong(), "SMALLINT")

	/**
	 * - **Signed Range** -8,388,608 to 8,388,607
	 * - **Unsigned Range** 0 to 16,777,215
	 */
	class MediumInt @JvmOverloads constructor(size : Int, override val unsigned : Boolean = false, override var primaryKey : Boolean = false, override var notNull : Boolean = true) : WholeNumberSqlType<Long>(size.toLong(), "MEDIUMINT")

	class BigInt @JvmOverloads constructor(size : String, override val unsigned : Boolean = false, override var primaryKey : Boolean = false, override var notNull : Boolean = true) : WholeNumberSqlType<BigInteger>(BigInteger(size), "BIGINT")

	/**
	 * Literally a [Float]/[Double]
	 */
	class Decimal @JvmOverloads constructor(size : Int = 10, decimals : Int = 0, override var primaryKey : Boolean = false, override var notNull : Boolean = true) : DecimalSqlType(size.toLong(), decimals, "DECIMAL")
	//endregion

	//region Character Types
	/**
	 * A fixed length [String], up to 255 characters
	 */
	class Char @JvmOverloads constructor(size : Int, override var primaryKey : Boolean = false, override var notNull : Boolean = true) : SizedSqlType<Long>(size.toLong(), "CHAR")

	/**
	 * A Variable length [String], up to 255 characters
	 */
	class VarChar @JvmOverloads constructor(size : Int, override var primaryKey : Boolean = false, override var notNull : Boolean = true) : SizedSqlType<Long>(size.toLong(), "VARCHAR")
	//endregion

	//region Misc Types
	/**
	 * A [Boolean], can be either true or false
	 */
	class Bool @JvmOverloads constructor(override var primaryKey : Boolean = false, override var notNull : Boolean = true) : SqlType("BOOLEAN")

	class EnumSet @JvmOverloads constructor(val enumClass : KClass<out Enum<*>>, override var primaryKey : Boolean = false, override var notNull : Boolean = true) : SqlType("ENUM") {

		override fun toString() : String {
			val constants = enumClass.java.enumConstants.joinToString(", ") { "'$it'" }
			return "$name($constants)${super.toString().substringAfter(name)}"
		}

	}

	class ValueSet @JvmOverloads constructor(val values : Array<String>, override var primaryKey : Boolean = false, override var notNull : Boolean = true) : SqlType("SET") {

		override fun toString() : String {
			val clampedValues = Array(64) { values[it] }.joinToString(", ") { "'$it'" }
			return "$name($clampedValues)${super.toString().substringAfter(name)}"
		}

	}

	class Timestamp @JvmOverloads constructor(val current : Boolean = false, val updating : Boolean = false, override var primaryKey : Boolean = false, override var notNull : Boolean = true) : SqlType("TIMESTAMP") {

		override fun toString() : String {
			var base = super.toString()
			if (current) base += CURRENT
			if (updating) base += "${if (current.not()) CURRENT else ""}${UPDATE}"

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

	class Like(column : String, data : Any, val option : LikeOption, var not : Boolean = false) : Where(column, data) {

		override fun toString() : String = "$column ${if (not) "NOT " else ""}LIKE ${option.block}"


		enum class LikeOption(val block : String) {

			Starts("?${WILDCARD}"),
			Ends("${WILDCARD}?"),
			Contains("${WILDCARD}?${WILDCARD}");

		}

		companion object {

			const val WILDCARD = '%'

		}

	}

	class Between(column : String, first : Any, val second : Any, var not : Boolean = false) : Where(column, first) {

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