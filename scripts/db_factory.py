#!/usr/bin/env python3
"""
========================================================================================
🐘 Enterprise Database Factory (DbFactory.py)
========================================================================================
Implements the exact architectural pattern of DbFactory.java with full typing,
connection lifecycle management, existence checks, session termination, and
database creation / destruction for PostgreSQL.
========================================================================================
"""
from __future__ import annotations

import os
import sys
import subprocess
from dataclasses import dataclass
from typing import Final, Optional

sys.path.insert(0, os.path.dirname(__file__))
from semantic_logger import SemanticLogger

logger: Final[SemanticLogger] = SemanticLogger("DbFactory")


@dataclass(frozen=True)
class DbContext:
    """Strongly-typed immutable database context configuration.
    
    Attributes:
        host: PostgreSQL server host (default: "localhost").
        port: PostgreSQL server port (default: 15432).
        database: Database name (default: "local_database").
        user: Database user (default: "local_user").
        password: Password credential (default: "local_password").
    """
    host: str = "localhost"
    port: int = 15432
    database: str = "local_database"
    user: str = "local_user"
    password: str = "local_password"

    def get_url(self) -> str:
        """Constructs the standard PostgreSQL connection URI."""
        return f"postgresql://{self.user}:{self.password}@{self.host}:{self.port}/{self.database}"


class DbFactory:
    """Enterprise Database Factory for PostgreSQL Lifecycle Management.
    
    Attributes:
        root_db_context: Root / maintenance connection context.
        session_killer: When true, actively terminates backend connections prior to DDL.
    """

    def __init__(self, root_db_context: DbContext, session_killer: bool = True) -> None:
        self.root_db_context: DbContext = root_db_context
        self.session_killer: bool = session_killer

    def does_database_exist(self, db_context: DbContext) -> bool:
        """Checks if the target database exists in pg_database."""
        existence_check = f"SELECT 1 AS count FROM pg_database WHERE datname='{db_context.database}';"
        return self._execute_count(existence_check) == 1

    def create(self, db_context: DbContext) -> None:
        """Provisions a new PostgreSQL database with explicit collation and privileges."""
        db_sql = (
            f"CREATE DATABASE {db_context.database}\n"
            f"  WITH OWNER = {db_context.user}\n"
            f"  TEMPLATE template0\n"
            f"  ENCODING = 'UTF8'\n"
            f"  LC_COLLATE = 'en_US.UTF-8'\n"
            f"  LC_CTYPE = 'en_US.UTF-8'\n"
            f"  CONNECTION LIMIT = -1;"
        )
        self.kill_session(db_context)
        self._execute(db_sql)

        grant_sql = f"GRANT ALL PRIVILEGES ON DATABASE {db_context.database} TO {db_context.user};"
        self._execute(grant_sql)

    def destroy(self, db_context: DbContext | str) -> None:
        """Destroys the specified database context or database name."""
        db_name = db_context.database if isinstance(db_context, DbContext) else db_context
        try:
            db_sql = f"DROP DATABASE IF EXISTS {db_name};"
            self._execute(db_sql)
        except Exception as e:
            logger.error(f"Error trying to destroy the database: {e}")

    def kill_session(self, db_context: DbContext) -> None:
        """Kills any active backend sessions connected to the specified database."""
        try:
            if self.session_killer:
                logger.info(f"=== Killing any active database connections as requested for `{db_context.database}`")
                session_killer_sql = (
                    f"SELECT pg_terminate_backend(pid) FROM pg_stat_activity "
                    f"WHERE datname = '{db_context.database}' AND pid <> pg_backend_pid();"
                )
                self._execute(session_killer_sql)
        except Exception as e:
            logger.error(f"Error trying to kill session: {e}")

    def _execute(self, sql: str) -> None:
        """Executes SQL against the PostgreSQL server via docker exec or psql."""
        logger.info(f"=== Running SQL:\n{sql}")
        cmd = [
            "docker", "exec", "-i", "local_postgres",
            "psql", "-U", self.root_db_context.user, "-d", self.root_db_context.database,
            "-c", sql
        ]
        res = subprocess.run(cmd, capture_output=True, text=True)
        if res.returncode != 0 and "already exists" not in res.stderr:
            logger.warn(f"SQL execution notice: {res.stderr.strip()}")
        logger.success("=== SQL Completed.")

    def _execute_count(self, sql: str) -> int:
        """Executes a scalar count SQL query."""
        logger.info(f"=== Running SQL:\n{sql}")
        cmd = [
            "docker", "exec", "-i", "local_postgres",
            "psql", "-U", self.root_db_context.user, "-d", self.root_db_context.database,
            "-t", "-A", "-c", sql
        ]
        res = subprocess.run(cmd, capture_output=True, text=True)
        out = res.stdout.strip()
        count = int(out) if out.isdigit() else 0
        logger.success(f"=== SQL Completed. (count={count})")
        return count
