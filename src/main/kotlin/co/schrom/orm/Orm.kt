package co.schrom.orm

import java.sql.Connection

interface Orm {
    val connection: Connection
}
