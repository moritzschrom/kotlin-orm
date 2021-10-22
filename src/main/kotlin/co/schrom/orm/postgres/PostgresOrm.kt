package co.schrom.orm.postgres

import co.schrom.orm.EntityMeta
import co.schrom.orm.Orm
import java.sql.Connection
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1

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

    override fun create(obj: Any): Boolean {
        // Create an EntityMeta model
        val entity = EntityMeta(obj::class)

        // Retrieve the SQL string
        val sql = PostgresOrmSql.insert(entity)

        val preparedStatement = connection.prepareStatement(sql)
        entity.fields.forEachIndexed(fun(index, field) {
            val property = field.property as KProperty1<Any, *>
            val value = property.get(obj)

            // Plus 1 because prepared statement index starts at 1
            preparedStatement.setObject(index + 1 , value)
        })
        val success = preparedStatement.execute()
        preparedStatement.close()

        return success
    }

    override fun delete(obj: Any): Boolean {
        // Create an EntityMeta model
        val entity = EntityMeta(obj::class)

        // Retrieve the SQL string
        val sql = PostgresOrmSql.delete(entity)

        val preparedStatement = connection.prepareStatement(sql)
        val property = entity.primaryKey.property as KProperty1<Any, *>
        preparedStatement.setObject(1 , property.get(obj))
        val success = preparedStatement.execute()
        preparedStatement.close()

        return success
    }

}
