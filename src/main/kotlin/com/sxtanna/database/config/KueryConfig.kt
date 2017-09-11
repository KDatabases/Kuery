package com.sxtanna.database.config


data class KueryConfig(val server : ServerOptions = ServerOptions(), val user : UserOptions = UserOptions(), val pool : PoolOptions = PoolOptions(), val debug : Boolean? = false) : DatabaseConfig {

	data class ServerOptions(val address : String = "", val port : Int = 3306, val database : String = "")

	data class UserOptions(val username : String = "", val password : String = "")

	data class PoolOptions(val size : Int = 15, val timeout : Int = 1000, val idle : Int = 10000)


	companion object {

		val DEFAULT = KueryConfig()

	}

}