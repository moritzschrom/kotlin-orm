package co.schrom.orm

import java.sql.Connection
import kotlin.reflect.KClass

interface Orm {
    val connection: Connection

    fun dropTableIfExists(kClass: KClass<*>)

    fun createTable(kClass: KClass<*>)

    fun createAssignmentTables(kClass: KClass<*>)

    fun dropAssignmentTablesIfExist(kClass: KClass<*>)

    fun create(obj: Any): Boolean

    fun update(obj: Any): Boolean

    fun delete(obj: Any): Boolean

    fun <T: Any> get(kClass: KClass<T>, primaryKey: Any): T?
}
