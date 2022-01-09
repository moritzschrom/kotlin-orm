package co.schrom.orm

interface OrmSql {

    fun typeDefinition(field: FieldMeta): String

    fun dropTableIfExists(entity: EntityMeta): String

    fun createTable(entity: EntityMeta): String

    fun createAssignmentTables(entity: EntityMeta): String

    fun dropAssignmentTablesIfExists(entity: EntityMeta): String

    fun insert(entity: EntityMeta): String

    fun update(entity: EntityMeta): String

    fun delete(entity: EntityMeta): String

    fun get(entity: EntityMeta): String

}
