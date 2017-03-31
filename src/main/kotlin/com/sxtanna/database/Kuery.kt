package com.sxtanna.database

import com.sxtanna.database.base.Database
import com.sxtanna.database.config.DatabaseConfig
import com.sxtanna.database.config.DatabaseConfigManager
import com.sxtanna.database.config.KueryConfig
import com.sxtanna.database.task.KueryTask
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import java.io.File
import java.sql.Connection

class Kuery(override val config : KueryConfig) : Database<Connection, KueryConfig, KueryTask>() {

	override val name : String = "Kuery"
	lateinit var pool : HikariDataSource
		private set


	override fun load() {
		val hikariConfig = HikariConfig().apply {
			jdbcUrl = "jdbc:mysql://${config.server.address}:${config.server.port}/${config.server.database}?useSSL=false"
			username = config.user.username
			password = config.user.password
			maximumPoolSize = config.pool.size
			connectionTimeout = config.pool.timeout.toLong()
			idleTimeout = config.pool.idle.toLong()
			isAutoCommit = true
		}

		pool = HikariDataSource(hikariConfig)
	}

	override fun poison() = pool.close()


	override fun poolResource() : Connection? = pool.connection

	override fun createTask(resource : Connection) : KueryTask = KueryTask(resource)


	companion object : DatabaseConfigManager<KueryConfig, Kuery> {

		@JvmStatic
		override fun get(file : File) : Kuery = Kuery(getConfig(file))

		@JvmStatic
		override fun getConfig(file : File) : KueryConfig {
			return DatabaseConfig.loadOrSave(file, KueryConfig.DEFAULT)
		}
	}

}