package co.schrom.orm.postgres

import co.schrom.orm.EntityMeta
import co.schrom.orm.Orm
import java.sql.Connection
import kotlin.reflect.KClass

class PostgresOrm(override val connection: Connection) : Orm {

    override fun dropTableIfExists(kClass: KClass<*>) {
        // Create an EntityMeta model
        val entity = EntityMeta(kClass)

        // Build the SQL string
        val sql = PostgresOrmSql.dropTableIfExists(entity)

        val statement = connection.createStatement()
        statement.execute(sql)
        statement.close()
    }

    override fun createTable(kClass: KClass<*>) {
        // Create an EntityMeta model
        val entity = EntityMeta(kClass)

        // Build the SQL string
        val sql = PostgresOrmSql.createTable(entity)

        val statement = connection.createStatement()
        statement.execute(sql)
        statement.close()
    }

}
