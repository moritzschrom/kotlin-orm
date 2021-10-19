package co.schrom.orm.postgres

import co.schrom.orm.EntityMeta
import co.schrom.orm.FieldMeta
import co.schrom.orm.OrmSql
import kotlin.reflect.full.createType

object PostgresOrmSql : OrmSql {

    override fun typeDefinition(field: FieldMeta): String {
        when(field.type) {
            Short::class.createType() -> return "smallint"
            Int::class.createType() -> return "integer"
            Long::class.createType() -> return "bigint"
            Float::class.createType() -> return "decimal(7,7)"
            Double::class.createType() -> return "decimal(14,14)"
            String::class.createType() -> return "varchar(" + field.length + ")"
            Boolean::class.createType() -> return "boolean"
        }
        throw Exception("Field type is not supported.")
    }

    override fun createTable(entity: EntityMeta): String {
        val builder = StringBuilder(" create table ").append(entity.table).append(" ( ")
        entity.fields.forEachIndexed(fun(index, field) {
            // Column
            builder.append(field.column).append(" ")

            // Type
            builder.append(typeDefinition(field)).append(" ")

            // Nullable
            if(field.isNullable) builder.append(" not null ")

            // Primary key
            if(field.isPrimaryKey) builder.append(" primary key ")

            // No comma for last column
            if(index < entity.fields.size - 1) builder.append(" , ")
        })
        builder.append(" ); ")

        return builder.toString()
    }

}
