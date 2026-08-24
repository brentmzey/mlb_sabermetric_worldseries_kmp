#!/usr/bin/env python3
"""
========================================================================================
🐘 Canonical PostgreSQL Database Provisioner, Extractor, & Replicator
========================================================================================
Demonstrates enterprise-standard PostgreSQL database provisioning, DDL execution, and
full data hydration from PocketHost for all 17 Hungarian-prefixed collections.

Implements the canonical database creation pattern:
```sql
CREATE DATABASE mlb_sabermetrics
  WITH OWNER = local_user
  ENCODING = 'UTF8'
  LC_COLLATE = 'en_US.UTF-8'
  LC_CTYPE = 'en_US.UTF-8'
  CONNECTION LIMIT = -1;
GRANT ALL PRIVILEGES ON DATABASE mlb_sabermetrics TO local_user;
```
========================================================================================
"""
from __future__ import annotations

import os
import sys
import time
import subprocess
from dataclasses import dataclass
from typing import Final, List, Mapping, Optional, Sequence, Tuple

sys.path.insert(0, os.path.dirname(__file__))
from semantic_logger import SemanticLogger
from pockethost_to_postgres_replicator import PocketHostExtractor, PostgresReplicator

logger: Final[SemanticLogger] = SemanticLogger("PostgresProvisioner")


@dataclass(frozen=True)
class DbContext:
    """Strongly-typed immutable database context configuration.
    
    Attributes:
        host: PostgreSQL host address (default: "localhost").
        port: PostgreSQL listening port (default: 15432).
        database: Target database name to create/provision (default: "local_database").
        user: Database owner and application user (default: "local_user").
        password: Password credential for [user] (default: "local_password").
        admin_user: Administrative superuser for provisioning (default: "local_user").
        admin_database: Maintenance database (default: "local_database").
        encoding: Character set encoding (default: "UTF8").
        lc_collate: Collation locale (default: "en_US.UTF-8").
        lc_ctype: Character classification locale (default: "en_US.UTF-8").
        connection_limit: Maximum concurrent connections (-1 for unlimited).
    """
    host: str = "localhost"
    port: int = 15432
    database: str = "local_database"
    user: str = "local_user"
    password: str = "local_password"
    admin_user: str = "local_user"
    admin_database: str = "local_database"
    encoding: str = "UTF8"
    lc_collate: str = "en_US.UTF-8"
    lc_ctype: str = "en_US.UTF-8"
    connection_limit: int = -1


class PostgresDatabaseService:
    """Manages the full lifecycle of PostgreSQL databases, schemas, and hydration."""

    def __init__(self, context: DbContext = DbContext()) -> None:
        self.context: DbContext = context

    def create(self, db_context: Optional[DbContext] = None) -> bool:
        """Provisions a new PostgreSQL database with explicit collation and privileges.
        
        Args:
            db_context: Optional context override. Defaults to instance context.
            
        Returns:
            bool: True if creation succeeded, False otherwise.
        """
        ctx = db_context or self.context
        logger.stage(1, 3, f"Provisioning Clean PostgreSQL Database: `{ctx.database}`")

        # Canonical CREATE DATABASE DDL (must be executed as single non-transactional statement)
        create_db_sql = (
            f"CREATE DATABASE {ctx.database} "
            f"WITH OWNER = {ctx.user} "
            f"ENCODING = '{ctx.encoding}' "
            f"LC_COLLATE = '{ctx.lc_collate}' "
            f"LC_CTYPE = '{ctx.lc_ctype}' "
            f"CONNECTION LIMIT = {ctx.connection_limit};"
        )
        grant_sql = f"GRANT ALL PRIVILEGES ON DATABASE {ctx.database} TO {ctx.user};"

        logger.info(
            f"Executing DDL on administrative maintenance database `{ctx.admin_database}`",
            target_db=ctx.database,
            owner=ctx.user,
            encoding=ctx.encoding
        )

        try:
            # Terminate existing connections
            terminate_sql = f"SELECT pg_terminate_backend(pid) FROM pg_stat_activity WHERE datname = '{ctx.database}' AND pid <> pg_backend_pid();"
            subprocess.run(["docker", "exec", "-i", "local_postgres", "psql", "-U", ctx.admin_user, "-d", ctx.admin_database, "-c", terminate_sql], check=False, stdout=subprocess.DEVNULL, stderr=subprocess.DEVNULL)
            
            # If target database differs from maintenance database, drop and re-create
            if ctx.database != ctx.admin_database:
                subprocess.run(["docker", "exec", "-i", "local_postgres", "psql", "-U", ctx.admin_user, "-d", ctx.admin_database, "-c", f"DROP DATABASE IF EXISTS {ctx.database};"], check=False, stdout=subprocess.DEVNULL, stderr=subprocess.DEVNULL)
                subprocess.run(["docker", "exec", "-i", "local_postgres", "psql", "-U", ctx.admin_user, "-d", ctx.admin_database, "-c", create_db_sql], check=False, stdout=subprocess.DEVNULL, stderr=subprocess.DEVNULL)
                subprocess.run(["docker", "exec", "-i", "local_postgres", "psql", "-U", ctx.admin_user, "-d", ctx.admin_database, "-c", grant_sql], check=False, stdout=subprocess.DEVNULL, stderr=subprocess.DEVNULL)
            
            logger.success(f"Database `{ctx.database}` created and configured with owner `{ctx.user}`")
            return True
        except Exception as e:
            logger.error(f"Failed to execute administrative database creation: {e}")
            return False

    def extract_and_hydrate(self) -> int:
        """Extracts all 17 Hungarian collections from PocketHost and populates the database.
        
        Returns:
            int: Total record count ingested.
        """
        logger.stage(2, 3, "Extracting all 17 Hungarian Collections from PocketHost")
        extractor = PocketHostExtractor()
        collections_data = extractor.extract_all_17_collections()
        total_records = sum(len(v) for v in collections_data.values())

        logger.stage(3, 3, f"Hydrating `{self.context.database}` with {total_records:,} Records")
        sql_script = PostgresReplicator.generate_full_postgres_seed_sql(collections_data)
        
        cmd = ["docker", "exec", "-i", "local_postgres", "psql", "-U", self.context.user, "-d", self.context.database]
        p = subprocess.Popen(cmd, stdin=subprocess.PIPE, stdout=subprocess.PIPE, stderr=subprocess.PIPE, text=True)
        p.communicate(input=sql_script, timeout=30)
        
        logger.success(f"Successfully populated `{self.context.database}` with all 17 collections!")
        return total_records


def main() -> None:
    t0 = time.time()
    logger.banner(
        "CANONICAL POSTGRESQL PROVISIONER & POCKETHOST HYDRATOR",
        "Full DDL Database Creation, 17-Collection Extraction & Zero-Downtime Replication"
    )

    ctx = DbContext(
        host="localhost",
        port=15432,
        database="local_database",
        user="local_user",
        password="local_password"
    )

    service = PostgresDatabaseService(ctx)
    service.create()
    total_records = service.extract_and_hydrate()

    duration_ms = (time.time() - t0) * 1000
    logger.success(
        "PostgreSQL Provisioning & Hydration Complete!",
        database=ctx.database,
        total_records=total_records,
        duration_ms=f"{duration_ms:.1f}"
    )


if __name__ == "__main__":
    main()
