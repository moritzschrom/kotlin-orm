package co.schrom.orm

interface OrmSql {

    fun typeDefinition(field: FieldMeta): String

    fun createTable(entity: EntityMeta): String

}
