package co.schrom.orm.postgres

import co.schrom.orm.EntityMeta
import co.schrom.orm.Orm
import co.schrom.orm.QueryMeta
import co.schrom.orm.annotations.RelationshipType
import java.sql.Connection
import java.sql.ResultSet
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

    override fun createAssignmentTables(kClass: KClass<*>) {
        // Create an EntityMeta model
        val entity = EntityMeta(kClass)

        // Build the SQL string
        val sql = PostgresOrmSql.createAssignmentTables(entity)

        val statement = connection.createStatement()
        statement.execute(sql)
        statement.close()
    }

    override fun dropAssignmentTablesIfExist(kClass: KClass<*>) {
        // Create an EntityMeta model
        val entity = EntityMeta(kClass)

        // Build the SQL string
        val sql = PostgresOrmSql.dropAssignmentTablesIfExists(entity)

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
        entity.internals.forEachIndexed(fun(index, field) {
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
        val fieldsWithoutPrimary = entity.internals.filter { field -> !field.isPrimaryKey }
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

        return createObjects(kClass, rs).firstOrNull()
    }

    override fun <T : Any> get(kClass: KClass<T>, query: QueryMeta<T>): Collection<T> {
        // Retrieve the SQL string and the parameters
        val sql = PostgresOrmSql.get(query)
        val parameters = query.getParameters()

        val preparedStatement = connection.prepareStatement(sql)
        for((index, param) in parameters.withIndex()) {
            preparedStatement.setObject(index + 1, param)
        }

        val rs = preparedStatement.executeQuery()
        return createObjects(kClass, rs)
    }

    override fun <T : Any> query(kClass: KClass<T>): QueryMeta<T> {
        val entity = EntityMeta(kClass)
        return QueryMeta(entity)
    }

    override fun beginTransaction(): Boolean {
        val sql = "BEGIN;"
        val preparedStatement = connection.prepareStatement(sql)
        val success = preparedStatement.execute()
        preparedStatement.close()
        return success
    }

    override fun commit(): Boolean {
        val sql = "COMMIT;"
        val preparedStatement = connection.prepareStatement(sql)
        val success = preparedStatement.execute()
        preparedStatement.close()
        return success
    }

    override fun rollback(): Boolean {
        val sql = "ROLLBACK;"
        val preparedStatement = connection.prepareStatement(sql)
        val success = preparedStatement.execute()
        preparedStatement.close()
        return success
    }

    fun <T : Any> createObjects(kClass: KClass<T>, rs: ResultSet): Collection<T> {
        val entity = EntityMeta(kClass)
        val constructor = kClass.primaryConstructor
        val fieldValues = emptyMap<KParameter, Any?>().toMutableMap()
        val list = arrayListOf<T>()

        while(rs.next() && constructor !== null) {
            var primaryKeyCache: Any? = null

            // For each internal field of the entity:
            entity.internals.forEachIndexed(fun(index, field) {
                val parameter = constructor.parameters.find { it.name === field.property.name }
                val _object = rs.getObject(index + 1)
                if(field.isPrimaryKey) {
                    primaryKeyCache = _object
                }
                fieldValues[parameter!!] = _object
            })

            // For each external field of the entity
            entity.externals.forEach(fun(field) {
                val parameter = constructor.parameters.find { it.name === field.property.name }
                val isMultiple = field.relationshipType === RelationshipType.MANY_TO_MANY || field.relationshipType === RelationshipType.ONE_TO_MANY

                val sql = PostgresOrmSql.getRelationship(entity, field)
                val preparedStatement = connection.prepareStatement(sql)
                preparedStatement.setObject(1, primaryKeyCache)
                val rsRelationships = preparedStatement.executeQuery()
                val objects = createObjects(field.relationshipEntity!!, rsRelationships)
                fieldValues[parameter!!] = if (isMultiple) {
                    objects
                } else {
                    objects.first()
                }
            })

            list.add(constructor.callBy(fieldValues))
        }

        return list;
    }

}
