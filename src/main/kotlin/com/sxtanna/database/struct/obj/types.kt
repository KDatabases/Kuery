package com.sxtanna.database.struct.obj

import java.math.BigInteger
import kotlin.reflect.KClass

sealed class SqlType(val name : String) {

	open protected var primaryKey : Boolean = false
	open protected var notNull : Boolean = true


	protected open fun name() = name

	override fun toString() : String = "${name()}${if (primaryKey) " PRIMARY KEY" else ""}${if (notNull) " NOT NULL" else ""}"


	//region Base Types
	abstract class TextSqlType(name : String) : SqlType(name)

	abstract class SizedSqlType<N>(val size : N, name : String) : SqlType(name) where N : Number, N : Comparable<N> {

		override fun name() : String = "$name(${size()})"

		open fun size() = size

		override fun toString() = name().let { "$it${super.toString().substringAfter(it)}" }

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

	class Text @JvmOverloads constructor(override var primaryKey : Boolean = false, override var notNull : Boolean = true) : TextSqlType("TEXT")

	class TinyText @JvmOverloads constructor(override var primaryKey : Boolean = false, override var notNull : Boolean = true) : TextSqlType("TINYTEXT")

	class MediumText @JvmOverloads constructor(override var primaryKey : Boolean = false, override var notNull : Boolean = true) : TextSqlType("MEDIUMTEXT")

	class LongText @JvmOverloads constructor(override var primaryKey : Boolean = false, override var notNull : Boolean = true) : TextSqlType("LONGTEXT")

	//endregion

	//region Misc Types
	/**
	 * A [Boolean], can be either true or false
	 */
	class Bool @JvmOverloads constructor(override var primaryKey : Boolean = false, override var notNull : Boolean = true) : SqlType("BOOLEAN")

	class EnumSet @JvmOverloads constructor(val enumClass : KClass<out Enum<*>>, override var primaryKey : Boolean = false, override var notNull : Boolean = true) : SqlType("ENUM") {

		override fun toString() = "$name(${enumClass.java.enumConstants.joinToString { "'$it'" }})${super.toString().substringAfter(name)}"

	}

	class ValueSet @JvmOverloads constructor(val values : Array<String>, override var primaryKey : Boolean = false, override var notNull : Boolean = true) : SqlType("SET") {

		override fun toString() = "$name(${Array(64) { values[it] }.joinToString { "'$it'" }})${super.toString().substringAfter(name)}"

	}

	class Timestamp @JvmOverloads constructor(private val current : Boolean = false, private val updating : Boolean = false, override var primaryKey : Boolean = false, override var notNull : Boolean = true) : SqlType("TIMESTAMP") {

		override fun toString() = buildString {
			append(super.toString())

			if (current) append(CURRENT)
			if (updating) append("${if (current.not()) CURRENT else ""}$UPDATE")
		}


		companion object {

			private const val UPDATE = " ON UPDATE CURRENT_TIMESTAMP"
			private const val CURRENT = " DEFAULT CURRENT_TIMESTAMP"

		}

	}
	//endregion

}

sealed class Duplicate {

	abstract fun invoke(key : String) : String


	class Ignore : Duplicate() {

		override operator fun invoke(key : String) : String = "ON DUPLICATE KEY UPDATE $key=$key"

	}

	class Update(private vararg val value : String) : Duplicate() {

		override fun invoke(key : String) : String = "ON DUPLICATE KEY UPDATE ${value.map { "$it=VALUES($it)" }.joinToString()}"

	}

}