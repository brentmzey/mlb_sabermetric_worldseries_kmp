package com.sabermetrics.worldseries.bootstrap

/**
 * Immutable Database Connection Context.
 *
 * Encapsulates all connection parameters required to connect to, provision, and configure
 * PostgreSQL instances within local-db-stack or remote database clusters.
 *
 * @property host Target host name or IP (default: "localhost").
 * @property port Target PostgreSQL port (default: 15432).
 * @property database Database name (e.g. "local_database" or "mlb_sabermetrics").
 * @property user Master or application user (default: "local_user").
 * @property password User credential (default: "local_password").
 */
data class DbContext(
    val host: String = "localhost",
    val port: Int = 15432,
    val database: String = "local_database",
    val user: String = "local_user",
    val password: String = "local_password"
) {
    /**
     * Constructs the standard PostgreSQL JDBC URL.
     */
    fun getUrl(): String = "jdbc:postgresql://$host:$port/$database"
}
