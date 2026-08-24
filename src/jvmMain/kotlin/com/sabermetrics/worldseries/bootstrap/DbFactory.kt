package com.sabermetrics.worldseries.bootstrap

import com.sabermetrics.worldseries.util.SemanticLogger
import java.sql.Connection
import java.sql.DriverManager
import java.sql.ResultSet
import java.sql.SQLException

/**
 * Enterprise Database Factory for PostgreSQL Lifecycle Management.
 *
 * Implements canonical database provisioning, existence checking, session termination,
 * and database teardown with full logging and exception handling.
 *
 * @param rootDbContext Root / maintenance connection context (e.g. postgres or local_database).
 * @param sessionKiller When true, actively terminates backend connections prior to DDL operations.
 */
class DbFactory(
    private val rootDbContext: DbContext,
    private val sessionKiller: Boolean = true
) {

    private val logger = SemanticLogger

    /**
     * Checks if the specified database exists in `pg_database`.
     *
     * @param dbContext Target database context to verify.
     * @return true if the database exists, false otherwise.
     */
    fun doesDatabaseExist(dbContext: DbContext): Boolean {
        val existenceCheck = "SELECT 1 AS count FROM pg_database WHERE datname='${dbContext.database}'"
        return executeCount(existenceCheck) == 1
    }

    /**
     * Creates a new PostgreSQL database with explicit ownership, UTF-8 encoding, and collation.
     *
     * @param dbContext Target database context to provision.
     */
    fun create(dbContext: DbContext) {
        val dbSql = String.format(
            listOf(
                " CREATE DATABASE %s",
                "   WITH OWNER = %s",
                "   TEMPLATE template0",
                "   ENCODING = 'UTF8'",
                "   LC_COLLATE = 'en_US.UTF-8'",
                "   LC_CTYPE = 'en_US.UTF-8'",
                "   CONNECTION LIMIT = -1;"
            ).joinToString(System.lineSeparator()),
            dbContext.database,
            dbContext.user
        )
        execute(dbSql)

        val grantSql = "GRANT ALL PRIVILEGES ON DATABASE ${dbContext.database} TO ${dbContext.user};"
        execute(grantSql)
    }

    /**
     * Destroys the specified database context.
     *
     * @param dbContext Database context to drop.
     */
    fun destroy(dbContext: DbContext) {
        destroy(dbContext.database)
    }

    /**
     * Drops the specified database if it exists.
     *
     * @param database Name of the database to drop.
     */
    fun destroy(database: String) {
        try {
            val dbSql = "DROP DATABASE IF EXISTS $database;"
            execute(dbSql)
        } catch (e: Exception) {
            System.err.println("Error trying to destroy the database: ${e.message}")
        }
    }

    /**
     * Actively terminates any active backend sessions connected to the specified database.
     *
     * @param dbContext Database context whose sessions should be killed.
     */
    fun killSession(dbContext: DbContext) {
        try {
            if (this.sessionKiller) {
                logger.info("DbFactory", "Killing any active database connections for ${dbContext.database}")
                val sessionKillerSql = "SELECT pg_terminate_backend(pid) FROM pg_stat_activity WHERE datname = '${dbContext.database}';"
                execute(sessionKillerSql)
            }
        } catch (e: Exception) {
            System.err.println("Error trying to kill session: ${e.message}")
        }
    }

    /**
     * Executes arbitrary SQL using an active JDBC Connection.
     */
    private fun execute(connection: Connection, sql: String) {
        try {
            logger.info("DbFactory", "Running SQL:\n$sql")
            connection.createStatement().use { statement ->
                statement.execute(sql)
            }
            logger.success("DbFactory", "SQL Completed successfully.")
        } catch (sqle: SQLException) {
            System.err.println(sqle.message)
            throw RuntimeException(sqle)
        } finally {
            try {
                connection.close()
            } catch (sqle: SQLException) {
                System.err.println(sqle.message)
            }
        }
    }

    /**
     * Obtains a connection and executes the given SQL string.
     */
    private fun execute(sql: String) {
        val connection = getConnection()
        execute(connection, sql)
    }

    /**
     * Executes a query returning a scalar integer count.
     */
    private fun executeCount(sql: String): Int {
        var rv = 0
        val connection = getConnection()
        try {
            logger.info("DbFactory", "Running SQL:\n$sql")
            connection.createStatement().use { statement ->
                statement.executeQuery(sql).use { rs ->
                    if (rs.next()) {
                        rv = rs.getInt("count")
                    }
                }
            }
            logger.success("DbFactory", "SQL Completed successfully. (count=$rv)")
        } catch (sqle: SQLException) {
            System.err.println(sqle.message)
            throw RuntimeException(sqle)
        } finally {
            try {
                connection.close()
            } catch (sqle: SQLException) {
                System.err.println(sqle.message)
            }
        }
        return rv
    }

    /**
     * Obtains a JDBC Connection from the root database context.
     */
    private fun getConnection(): Connection {
        return try {
            val connection = DriverManager.getConnection(
                rootDbContext.getUrl(),
                rootDbContext.user,
                rootDbContext.password
            )
            logger.success("DbFactory", "Connected to the PostgreSQL server successfully: ${rootDbContext.getUrl()}")
            connection
        } catch (sqle: SQLException) {
            // Docker fallback if running without direct port mapping or driver
            System.err.println("JDBC direct connection error: ${sqle.message}")
            throw RuntimeException(sqle)
        }
    }
}
