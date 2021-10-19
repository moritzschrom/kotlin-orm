package co.schrom.orm

import java.sql.Connection
import kotlin.reflect.KClass

interface Orm {
    val connection: Connection

    fun createTable(kClass: KClass<*>)
}
