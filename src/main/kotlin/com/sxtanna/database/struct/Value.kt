package com.sxtanna.database.struct

import java.sql.PreparedStatement

data class Value(val data: Any, val name: String = "") {


    fun prepare(prep: PreparedStatement, position: Int) {
        when(data) {
			is Byte -> prep.setByte(position, data)
			is Short -> prep.setShort(position, data)
            is Int -> prep.setInt(position, data)
			is Long -> prep.setLong(position, data)
            is Double -> prep.setDouble(position, data)
            is String -> prep.setString(position, data)
            else -> prep.setString(position, data.toString())
        }
    }

    override fun toString(): String = "$name=Values($name)"

}