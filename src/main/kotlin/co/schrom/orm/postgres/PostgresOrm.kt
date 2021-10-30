package co.schrom.orm.postgres

import co.schrom.orm.EntityMeta
import co.schrom.orm.Orm
import java.sql.Connection
import kotlin.reflect.KClass
import kotlin.reflect.KParameter
import kotlin.reflect.KProperty1
import kotlin.reflect.full.primaryConstructor

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

    override fun update(obj: Any): Boolean {
        // Create an EntityMeta model
        val entity = EntityMeta(obj::class)

        // Retrieve the SQL string
        val sql = PostgresOrmSql.update(entity)

        val preparedStatement = connection.prepareStatement(sql)

        // First set the normal fields
        val fieldsWithoutPrimary = entity.fields.filter { field -> !field.isPrimaryKey }
        var index = 1
        fieldsWithoutPrimary.forEach(fun(field) {
            val property = field.property as KProperty1<Any, *>
            val value = property.get(obj)

            // Plus 1 because prepared statement index starts at 1
            preparedStatement.setObject(index++ , value)
        })

        // Then set the primary key as last argument
        val primaryProperty = entity.primaryKey.property as KProperty1<Any, *>
        val primaryValue = primaryProperty.get(obj)
        preparedStatement.setObject(index, primaryValue)

        // Execute
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

    override fun <T : Any> get(kClass: KClass<T>, primaryKey: Any): T? {
        // Create an EntityMeta model
        val entity = EntityMeta(kClass)

        // Retrieve the SQL string
        val sql = PostgresOrmSql.get(entity)

        val preparedStatement = connection.prepareStatement(sql)
        preparedStatement.setObject(1, primaryKey)
        val rs = preparedStatement.executeQuery()

        val constructor = kClass.primaryConstructor
        val fieldValues = emptyMap<KParameter, Any?>().toMutableMap()

        // Return null when the result set is empty or there is no constructor
        if (!rs.next() || constructor === null) return null

        // For each field of the entity:
        // Read the data from the result set and set it to the parameters map
        entity.fields.forEachIndexed(fun(index, field) {
            val parameter = constructor.parameters.find { it.name === field.property.name }
            fieldValues[parameter!!] = rs.getObject(index + 1)
        })

        return constructor.callBy(fieldValues)
    }

}
