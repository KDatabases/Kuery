package com.sxtanna.database.struct

import com.sxtanna.database.struct.obj.SqlType
import com.sxtanna.database.struct.obj.SqlType.NormInt

/**
 * Begin working on this as a replacement to requiring users create their own objects
 */
enum class SqlTypes {

	INT {

		@JvmOverloads
		fun of(size : Long, unsigned : Boolean = false,  primaryKey : Boolean = false, notNull : Boolean = true) : SqlType.NormInt {
			return NormInt(size, unsigned, primaryKey, notNull)
		}

	},
	TINYINT,
	SMALLINT,
	MEDIUMINT,
	BIGINT,

	DECIMAL,

	CHAR,
	VARCHAR,

	TEXT,
	TINYTEXT,
	MEDIUMTEXT,
	LONGTEXT,

	BOOL,
	ENUM,
	VALUE,
	TIMESTAMP;

}