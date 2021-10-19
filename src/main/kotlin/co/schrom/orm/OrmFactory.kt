package co.schrom.orm

import java.sql.Connection
import java.sql.DriverManager

abstract class OrmFactory {

    abstract fun createOrm(connection: Connection): Orm

    fun createOrm(url: String, user: String, password: String): Orm {
        val conn = DriverManager.getConnection(url, user, password)
        return this.createOrm(conn)
    }

}
