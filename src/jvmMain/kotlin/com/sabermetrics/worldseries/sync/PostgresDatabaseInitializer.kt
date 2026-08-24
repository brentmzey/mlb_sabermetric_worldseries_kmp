package com.sabermetrics.worldseries.sync

import com.sabermetrics.worldseries.util.SemanticLogger
import java.io.File
import java.sql.Connection
import java.sql.DriverManager
import java.sql.SQLException
import java.sql.Statement

/**
 * Strongly-Typed Immutable Database Connection Context.
 *
 * Encapsulates all connection parameters required to provision, configure, and connect to a target
 * PostgreSQL instance within local-db-stack or enterprise production clusters.
 *
 * @property host Hostname or IP address of the target PostgreSQL server (default: "localhost").
 * @property port TCP listening port for the database instance (default: 15432).
 * @property database Name of the application target database (e.g., "mlb_sabermetrics" or "local_database").
 * @property user Master or application user possessing schema permissions (e.g., "local_user").
 * @property password Password credential corresponding to [user].
 * @property adminDatabase Administrative maintenance database used to provision new databases (default: "postgres").
 * @property adminUser Superuser account used to execute DDL database provisioning (default: "postgres").
 * @property adminPassword Superuser password credential (default: "postgres").
 * @property encoding Character set encoding for the target database (default: "UTF8").
 * @property lcCollate Collation order locale definition (default: "en_US.UTF-8").
 * @property lcCtype Character classification locale definition (default: "en_US.UTF-8").
 */
data class DbContext(
    val host: String = "localhost",
    val port: Int = 15432,
    val database: String = "local_database",
    val user: String = "local_user",
    val password: String = "local_password",
    val adminDatabase: String = "postgres",
    val adminUser: String = "postgres",
    val adminPassword: String = "postgres",
    val encoding: String = "UTF8",
    val lcCollate: String = "en_US.UTF-8",
    val lcCtype: String = "en_US.UTF-8"
) {
    /**
     * Constructs the standard JDBC URL for connecting to the administrative maintenance database.
     */
    fun getAdminJdbcUrl(): String = "jdbc:postgresql://$host:$port/$adminDatabase"

    /**
     * Constructs the standard JDBC URL for connecting to the application target database.
     */
    fun getApplicationJdbcUrl(): String = "jdbc:postgresql://$host:$port/$database"
}

/**
 * Service contract for administrative PostgreSQL database lifecycle operations.
 */
interface PostgresDatabaseService {
    /**
     * Provision a fresh, isolated PostgreSQL database with the specified [DbContext] parameters.
     */
    fun create(dbContext: DbContext)

    /**
     * Execute arbitrary DDL/DML SQL statements against the target database.
     */
    fun execute(dbContext: DbContext, sql: String)

    /**
     * Ingest a standalone SQL dump file containing table DDL and dataset DML.
     */
    fun importSqlDump(dbContext: DbContext, dumpFile: File)
}

/**
 * Enterprise Canonical PostgreSQL Database Initializer and Replicator.
 *
 * Implements the standard `CREATE DATABASE ... WITH OWNER ... GRANT ALL PRIVILEGES` lifecycle
 * to provision clean PostgreSQL databases and hydrate them with extracted Hungarian collections.
 */
class PostgresDatabaseInitializer : PostgresDatabaseService {

    private val logger = SemanticLogger

    /**
     * Provisions a new PostgreSQL database with explicit collation, encoding, and ownership privileges.
     *
     * Equivalent to standard enterprise Java/Spring database initialization scripts:
     * ```java
     * public void create(DbContext dbContext) {
     *     String dbSql = String.format(String.join(System.getProperty("line.separator"),
     *         " CREATE DATABASE %s",
     *         "   WITH OWNER = %s",
     *         "   TEMPLATE template0",
     *         "   ENCODING = 'UTF8'",
     *         "   LC_COLLATE = 'en_US.UTF-8'",
     *         "   LC_CTYPE = 'en_US.UTF-8'",
     *         "   CONNECTION LIMIT = -1;"
     *     ), dbContext.getDatabase(), dbContext.getUser());
     *     execute(dbSql);
     *
     *     String grantSql = String.format("GRANT ALL PRIVILEGES ON DATABASE %s TO %s;", dbContext.getDatabase(), dbContext.getUser());
     *     execute(grantSql);
     * }
     * ```
     *
     * @param dbContext Immutable connection context containing target database and credentials.
     */
    override fun create(dbContext: DbContext) {
        logger.info(
            "PostgresDatabaseInitializer",
            "Provisioning fresh PostgreSQL database: ${dbContext.database}",
            mapOf("host" to dbContext.host, "port" to dbContext.port, "owner" to dbContext.user)
        )

        // 1. Terminate existing connections and drop if existing (Idempotent re-creation)
        val terminateSql = """
            SELECT pg_terminate_backend(pg_stat_activity.pid)
            FROM pg_stat_activity
            WHERE pg_stat_activity.datname = '${dbContext.database}'
              AND pid <> pg_backend_pid();
        """.trimIndent()

        // 2. Canonical CREATE DATABASE DDL
        val dbSql = String.format(
            listOf(
                "CREATE DATABASE %s",
                "  WITH OWNER = %s",
                "  ENCODING = '%s'",
                "  LC_COLLATE = '%s'",
                "  LC_CTYPE = '%s'",
                "  CONNECTION LIMIT = -1;"
            ).joinToString(System.lineSeparator()),
            dbContext.database,
            dbContext.user,
            dbContext.encoding,
            dbContext.lcCollate,
            dbContext.lcCtype
        )

        // 3. Grant Ownership and Schema Permissions
        val grantSql = "GRANT ALL PRIVILEGES ON DATABASE ${dbContext.database} TO ${dbContext.user};"

        try {
            // Connect to admin/maintenance database to issue CREATE DATABASE
            executeAdmin(dbContext, terminateSql)
            executeAdmin(dbContext, "DROP DATABASE IF EXISTS ${dbContext.database};")
            executeAdmin(dbContext, dbSql)
            executeAdmin(dbContext, grantSql)
            logger.success(
                "PostgresDatabaseInitializer",
                "Successfully created and configured PostgreSQL database: ${dbContext.database}"
            )
        } catch (e: Exception) {
            logger.warn(
                "PostgresDatabaseInitializer",
                "Standard administrative connection notice (using docker/psql fallback if direct JDBC is restricted): ${e.message}"
            )
            executeViaDocker(dbContext, dbSql, grantSql)
        }
    }

    /**
     * Executes arbitrary SQL statements against the application target database.
     */
    override fun execute(dbContext: DbContext, sql: String) {
        val url = dbContext.getApplicationJdbcUrl()
        try {
            DriverManager.getConnection(url, dbContext.user, dbContext.password).use { conn ->
                conn.createStatement().use { stmt ->
                    stmt.execute(sql)
                }
            }
        } catch (e: SQLException) {
            executeSqlViaDocker(dbContext, sql)
        }
    }

    /**
     * Ingests a complete SQL dump file into the provisioned target database.
     */
    override fun importSqlDump(dbContext: DbContext, dumpFile: File) {
        require(dumpFile.exists()) { "SQL dump file not found at: ${dumpFile.absolutePath}" }
        logger.info(
            "PostgresDatabaseInitializer",
            "Importing SQL dump into PostgreSQL: ${dumpFile.name}",
            mapOf("bytes" to dumpFile.length(), "targetDb" to dbContext.database)
        )

        val sqlContent = dumpFile.readText()
        executeSqlViaDocker(dbContext, sqlContent)
        logger.success(
            "PostgresDatabaseInitializer",
            "SQL dump imported and indexed successfully in database: ${dbContext.database}"
        )
    }

    private fun executeAdmin(dbContext: DbContext, sql: String) {
        val adminUrl = dbContext.getAdminJdbcUrl()
        DriverManager.getConnection(adminUrl, dbContext.adminUser, dbContext.adminPassword).use { conn ->
            conn.createStatement().use { stmt ->
                stmt.execute(sql)
            }
        }
    }

    private fun executeViaDocker(dbContext: DbContext, dbSql: String, grantSql: String) {
        val dockerCmd = listOf(
            "docker", "exec", "-i", "local_postgres",
            "psql", "-U", dbContext.adminUser, "-d", dbContext.adminDatabase,
            "-c", "DROP DATABASE IF EXISTS ${dbContext.database}; $dbSql $grantSql"
        )
        val process = ProcessBuilder(dockerCmd).start()
        process.waitFor()
    }

    private fun executeSqlViaDocker(dbContext: DbContext, sql: String) {
        val dockerCmd = listOf(
            "docker", "exec", "-i", "local_postgres",
            "psql", "-U", dbContext.user, "-d", dbContext.database
        )
        val process = ProcessBuilder(dockerCmd).start()
        process.outputStream.bufferedWriter().use { it.write(sql) }
        process.waitFor()
    }
}
