package com.sxtanna.database.struct

import com.sxtanna.database.ext.*
import java.sql.PreparedStatement
import kotlin.reflect.KClass

sealed class SqlType(val name: String) {

	open val primaryKey: Boolean = false
	open val notNull: Boolean = true


	protected open fun name() = name


    override fun toString(): String = "${name()}${if (primaryKey) " Primary Key" else ""}${if (notNull) " Not Null" else ""}"


	//region Base Types
	abstract class SizedSqlType(val size: Int, name: String): SqlType(name) {

		override fun name() : String = "$name(${size()})"

		open fun size() = size

        override fun toString(): String {
			val outputName = name()
			return "$outputName${super.toString().substringAfter(outputName)}"
		}

    }

	abstract class NumberSqlType(size : Int, name : String) : SizedSqlType(size, name) {

		open val unsigned: Boolean = false


		open fun numberName() = super.name()

		override fun size() : Int = super.size().coerceIn(range())

		override fun name() : String = "${numberName()}${if (unsigned) " UNSIGNED" else ""}"


		abstract fun range(): IntRange


	}

	abstract class DecimalSqlType(fullSize: Int, val decimals: Int, name : String) : NumberSqlType(fullSize, name) {

		override fun numberName() : String = "$name(${size()}, $decimals)"

		override fun range() : IntRange = 0..30

	}
	//endregion


	//region Number Types
	class TinyInt(size : Int, override val unsigned: Boolean = false, override val primaryKey : Boolean = false, override val notNull : Boolean = true) : NumberSqlType(size, "TINYINT") {

		override fun range() : IntRange = if (unsigned) MIN_UNSIGN..TINY_MAX_UNSIGN else TINY_MIN_SIGN..TINY_MAX_SIGN

	}

	class SmallInt(size : Int, override val unsigned: Boolean = false, override val primaryKey : Boolean = false, override val notNull : Boolean = true) : NumberSqlType(size, "SMALLINT") {

		override fun range() : IntRange = if (unsigned) MIN_UNSIGN..SMALL_MAX_UNSIGN else SMALL_MIN_SIGN..SMALL_MAX_SIGN

	}

	class Decimal(size : Int = 10, decimals : Int = 0, override val primaryKey : Boolean = false, override val notNull : Boolean = true) : DecimalSqlType(size, decimals, "DECIMAL")
	//endregion


	//region Character Types
	class VarChar(size: Int, override val primaryKey: Boolean = false, override val notNull: Boolean = true) : SizedSqlType(size, "VARCHAR")

	class Char(size: Int, override val primaryKey: Boolean = false, override val notNull: Boolean = true) : SizedSqlType(size, "CHAR")
	//endregion


	//region Misc Types
	class Bool(override val primaryKey: Boolean = false, override val notNull: Boolean = true) : SqlType("BOOLEAN")

    class EnumSet<E: Enum<E>>(val enumClass: KClass<E>, override val primaryKey: Boolean = false, override val notNull: Boolean = true) : SqlType("ENUM") {

		override fun toString() : String {
			val constants = enumClass.java.enumConstants.joinToString(", ") { "'$it'" }
			return "$name($constants)${super.toString().substringAfter(name)}"
		}

	}
	//endregion

}

sealed class Duplicate {

    abstract fun invoke(key: String): String


    class Ignore : Duplicate() {

        override operator fun invoke(key: String): String = "On Duplicate Key Update $key=$key"

    }

    class Update(vararg val value: String) : Duplicate() {

        override fun invoke(key: String): String = "On Duplicate Key Update ${value.map{ "$it=Values($it)" }.joinToString(", ")}"

    }

}

sealed class Where(val column: String, val data: Any) {

    open fun prepare(prep: PreparedStatement, position: Int) {
        set(prep, position, data)
    }

    protected fun set(prep: PreparedStatement, position: Int, data: Any) {
        when(data) {
            is Int -> prep.setInt(position, data)
            is Double -> prep.setDouble(position, data)
            is String -> prep.setString(position, data)
            else -> prep.setString(position, data.toString())
        }
    }


    open class RelationalWhere(column: String, data: Any, val orEqual: Boolean, val symbol: Char) : Where(column, data) {

        override fun toString(): String = "$column $symbol${if (orEqual) "=" else ""} ?"

    }


    class Equals(column: String, data: Any) : Where(column, data) {

        override fun toString(): String = "$column=?"

    }

    class Like(column: String, data: Any, val option: LikeOption, val not: Boolean = false) : Where(column, data) {

        override fun toString(): String = "$column ${if (not) "NOT " else ""}LIKE ${option.block}"


        enum class LikeOption(val block: String) {

            Starts   ("?$WILDCARD"),
            Ends     ("$WILDCARD?"),
            Contains ("$WILDCARD?$WILDCARD");

        }

        companion object {

            const val WILDCARD = '%'

        }

    }

    class Between(column: String, first: Any, val second: Any, val not: Boolean = false) : Where(column, first) {

        override fun prepare(prep: PreparedStatement, position: Int) {
            set(prep, position, data)
            set(prep, position + 1, second)
        }

        override fun toString(): String  = "$column ${if (not) "NOT " else ""}BETWEEN ? AND ?"

    }

    class Greater(column: String, data: Any, orEqual: Boolean = false) : RelationalWhere(column, data, orEqual, '>')

    class Less(column: String, data: Any, orEqual: Boolean = false) : RelationalWhere(column, data, orEqual, '<')

}