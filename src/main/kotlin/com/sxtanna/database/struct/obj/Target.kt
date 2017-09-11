package com.sxtanna.database.struct.obj

import com.sxtanna.database.ext.value
import com.sxtanna.database.struct.obj.Target.Position.*
import java.sql.PreparedStatement

sealed class Target {

	protected abstract val data : Any?
	protected abstract val column : String

	protected open var not : Boolean = false


	override fun toString() = "$column${if (not) "!" else ""}=?"


	open fun data() = data

	fun not() = apply { this.not = true }


	internal open fun prep(statement : PreparedStatement, pos : Int) = statement.setObject(pos, data()).let { 0 }


	private class Equal(override var not : Boolean, override val data : Any?, override val column : String) : Target()

	private class Between(override var not : Boolean, override val data : Any, private val other : Any, override val column : String) : Target() {

		override fun prep(statement : PreparedStatement, pos : Int) : Int {
			statement.setObject(pos, data)
			statement.setObject(pos + 1, other)

			return 1
		}

		override fun toString() = "$column ${not.value("NOT ")}BETWEEN ? AND ?"

	}

	private class Like(private val pos : Position, override var not : Boolean, override val data : Any, override val column : String) : Target() {

		override fun data() = pos.place.replace("?", data.toString())

		override fun toString() = "$column ${not.value("NOT ")}LIKE ?"

	}


	abstract class Relational(private val orEqual : Boolean, private val symbol : Char) : Target() {

		override final var not = false

		override fun toString() = "$column $symbol${orEqual.value("=")} ?"

	}

	private class Lesser(orEqual : Boolean, override val data : Any, override val column : String) : Relational(orEqual, '<')

	private class Greater(orEqual : Boolean, override val data : Any, override val column : String) : Relational(orEqual, '>')


	enum class Position(val place : String) {

		END      ("%?"),
		START    ("?%"),
		CONTAINS ("%?%")

	}


	companion object Where {

		@JvmSynthetic
		operator fun <R> invoke(block : Where.() -> R) = this.block()

		@JvmSynthetic
		operator fun get(vararg target : Target) = target


		//region Equals and Between
		@JvmStatic
		@JvmOverloads
		fun equals(column : String, data : Any?, not : Boolean = false) : Target = Equal(not, data, column)

		@JvmSynthetic
		@JvmName("equalW")
		infix fun String.equals(data : Any?) = equals(this, data)


		@JvmStatic
		@JvmOverloads
		fun <O : Any> between(column : String, first : O, second : O, not : Boolean = false) : Target = Between(not, first, second, column)

		@JvmSynthetic
		infix fun <O : Any> String.between(data : Pair<O, O>) = between(this, data.first, data.second)
		//endregion


		//region Likes
		@JvmStatic
		@JvmOverloads
		fun like(column : String, data : Any, pos : Position, not : Boolean = false) : Target = Like(pos, not, data, column)


		@JvmStatic
		@JvmOverloads
		fun ends(column : String, data : Any, not : Boolean = false) = like(column, data, END, not)

		@JvmSynthetic
		@JvmName("endW")
		infix fun String.ends(data : Any) = ends(this, data)


		@JvmStatic
		@JvmOverloads
		fun starts(column : String, data : Any, not : Boolean = false) = like(column, data, START, not)

		@JvmSynthetic
		@JvmName("startW")
		infix fun String.starts(data :  Any) = starts(this, data)


		@JvmStatic
		@JvmOverloads
		fun contains(column : String, data : Any, not : Boolean = false) = like(column, data, CONTAINS, not)

		@JvmSynthetic
		@JvmName("containW")
		infix fun String.contains(data : Any) = contains(this, data)
		//endregion


		//region Relationals
		@JvmStatic
		@JvmOverloads
		fun lesser(column : String, data : Any, orEqual : Boolean = false) : Relational = Lesser(orEqual, data, column)

		@JvmStatic
		@JvmOverloads
		fun greater(column : String, data : Any, orEqual : Boolean = false) : Relational = Greater(orEqual, data, column)
		//endregion

	}

}