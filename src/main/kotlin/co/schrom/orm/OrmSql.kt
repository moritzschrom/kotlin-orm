package co.schrom.orm

interface OrmSql {

    fun typeDefinition(field: FieldMeta): String

    fun dropTableIfExists(entity: EntityMeta): String

    fun createTable(entity: EntityMeta): String

    fun insert(entity: EntityMeta): String

    fun delete(entity: EntityMeta): String

}
