package com.sxtanna.database.struct.base

import com.sxtanna.database.type.base.SqlObject
import java.sql.ResultSet
import java.util.function.Function
import kotlin.reflect.KClass

abstract class Creator<O : SqlObject>(val clazz : KClass<O>) : (ResultSet) -> O, Function<ResultSet, O> {
	constructor(clazz : Class<O>) : this(clazz.kotlin)


	override final fun invoke(rs : ResultSet) = apply(rs)

}