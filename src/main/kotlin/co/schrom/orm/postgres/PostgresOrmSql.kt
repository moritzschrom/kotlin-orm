package co.schrom.orm.postgres

import co.schrom.orm.*
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

    override fun dropTableIfExists(entity: EntityMeta): String {
        val builder = StringBuilder("drop table if exists ").append(entity.table).append(" cascade;")
        return builder.toString()
    }

    override fun createTable(entity: EntityMeta): String {
        val builder = StringBuilder("create table ").append(entity.table).append("( ")
        entity.internals.forEachIndexed(fun(index, field) {
            // Column
            builder.append(field.column).append(" ")

            // Type
            builder.append(typeDefinition(field)).append(" ")

            // Nullable
            if(!field.isNullable) builder.append("not null ")

            // Primary key
            if(field.isPrimaryKey) builder.append("primary key ")

            // No comma for last column
            if(index < entity.internals.size - 1) builder.append(", ")
        })
        builder.append(");")

        return builder.toString()
    }

    override fun createAssignmentTables(entity: EntityMeta): String {
        val builder = StringBuilder()
        entity.externals.forEach(fun(field) {
            val relationship = field.relationshipEntity?.let { EntityMeta(it) }
            if(relationship !is EntityMeta) return

            builder.append("create table if not exists ").append(field.assignmentTable).append("( ")
            builder.append("id serial primary key, ")

            val entityColumn = StringBuilder(entity.table).append("_").append(entity.primaryKey.column).toString()
            val relationshipColumn = StringBuilder(relationship.table).append("_").append(relationship.primaryKey.column).toString()

            // Entity
            builder.append(entityColumn).append(" ").append(typeDefinition(entity.primaryKey)).append(" not null,")
            // Relationship
            builder.append(relationshipColumn).append(" ").append(typeDefinition(relationship.primaryKey)).append(" not null,")

            // Constraints
            builder.append("constraint fk_").append(entity.table).append(" ")
                .append("foreign key(").append(entityColumn).append(")")
                .append("references ").append(entity.table).append("(").append(entity.primaryKey.column).append("),")

            builder.append("constraint fk_").append(relationship.table).append(" ")
                .append("foreign key(").append(entityColumn).append(")")
                .append("references ").append(relationship.table).append("(").append(relationship.primaryKey.column).append(")")
            builder.append(");")
        })
        return builder.toString()
    }

    override fun dropAssignmentTablesIfExists(entity: EntityMeta): String {
        val builder = StringBuilder()
        entity.externals.forEach(fun(field) {
            builder.append("drop table if exists ").append(field.assignmentTable).append(";")
        })
        return builder.toString()
    }

    override fun insert(entity: EntityMeta): String {
        val builder = StringBuilder("insert into ").append(entity.table).append("( ")
        entity.internals.forEachIndexed(fun(index, field) {
            builder.append(field.column).append(" ")
            // No comma for last column
            if(index < entity.internals.size - 1) builder.append(", ")
        })
        builder.append(") values( ")
        repeat(entity.internals.size - 1) { builder.append("? , ") }
        builder.append("? );")

        return builder.toString()
    }

    override fun update(entity: EntityMeta): String {
        val builder = StringBuilder("update ").append(entity.table).append(" set ")
        val fieldsWithoutPrimary = entity.internals.filter { field -> !field.isPrimaryKey }
        fieldsWithoutPrimary.forEachIndexed(fun(index, field) {
            builder.append(field.column).append(" = ? ")
            // No comma for last column
            if(index < fieldsWithoutPrimary.size - 1) builder.append(", ")
        })
        builder.append("where ").append(entity.primaryKey.column).append(" = ?;")

        return builder.toString()
    }

    override fun delete(entity: EntityMeta): String {
        val builder = StringBuilder("delete from ")
            .append(entity.table)
            .append(" where ")
            .append(entity.primaryKey.column)
            .append(" = ?;")

        return builder.toString()
    }

    override fun get(entity: EntityMeta): String {
        val builder = StringBuilder("select * from ")
            .append(entity.table)
            .append(" where ")
            .append(entity.primaryKey.column)
            .append(" = ?;")

        return builder.toString()
    }

    override fun get(query: QueryMeta<*>): String {
        val operations = arrayListOf<QueryMeta<*>>()
        val entity = query.entity

        var q: QueryMeta<*>? = query
        while(q != null) {
            operations.add(0, q)
            q = q.previous
        }

        val builder = StringBuilder("select * from ")
            .append(entity.table)

        var conj = " where "
        var not = false
        var openBracket = StringBuilder()
        var closeBracket = StringBuilder()
        var op: String

        for(operation in operations) {
            when(operation.operation) {

                QueryOperation.OR -> {
                    if(conj != " where ") conj = " or "
                }

                QueryOperation.NOT -> {
                    not = true
                }

                QueryOperation.GROUP -> {
                    openBracket.append("(")
                }

                QueryOperation.END_GROUP -> {
                    openBracket.append(")")
                }

                QueryOperation.EQUALS -> {
                    val field = entity.getFieldByName(operation.args!![0] as String)
                    val ignoreCase = operation.args!![2] as Boolean
                    op = if (not) " != "  else " ); "
                    builder
                        .append(closeBracket)
                        .append(conj)
                        .append(openBracket)
                        .append(if (ignoreCase) " lower(" + (field!!.column) + ")" else field!!.column)
                        .append(op)
                        .append(if (ignoreCase) "lower(?)" else "?")

                    openBracket = StringBuilder()
                    closeBracket = StringBuilder()
                    conj = " and "
                    not = false
                }

                QueryOperation.LIKE -> {
                    val field = entity.getFieldByName(operation.args!![0] as String)
                    val ignoreCase = operation.args!![2] as Boolean
                    op = if (not) " not like "  else " like "
                    builder
                        .append(closeBracket)
                        .append(conj)
                        .append(openBracket)
                        .append(if (ignoreCase) " lower(" + (field!!.column) + ")" else field!!.column)
                        .append(op)
                        .append(if (ignoreCase) "lower(?)" else "?")

                    openBracket = StringBuilder()
                    closeBracket = StringBuilder()
                    conj = " and "
                    not = false
                }

                QueryOperation.IN -> {
                    val field = entity.getFieldByName(operation.args!![0] as String)
                    println(field)
                    builder
                        .append(closeBracket)
                        .append(conj)
                        .append(openBracket)
                        .append(field!!.column)
                        .append(if (not) " not in ( " else " in ( ")

                    for(i in 1 until operation.args!!.size) {
                        if(i > 1) builder.append(" , ")
                        builder.append(" ? ")
                    }
                    builder.append(" ) ")

                    openBracket = StringBuilder()
                    closeBracket = StringBuilder()
                    conj = " and "
                    not = false
                }

                QueryOperation.GT -> {
                    val field = entity.getFieldByName(operation.args!![0] as String)
                    op = if (not) " <= "  else " > "
                    builder
                        .append(closeBracket)
                        .append(conj)
                        .append(openBracket)
                        .append(field!!.column)
                        .append(op)
                        .append(" ? ")

                    openBracket = StringBuilder()
                    closeBracket = StringBuilder()
                    conj = " and "
                    not = false
                }

                QueryOperation.LT -> {
                    val field = entity.getFieldByName(operation.args!![0] as String)
                    op = if (not) " >= "  else " < "
                    builder
                        .append(closeBracket)
                        .append(conj)
                        .append(openBracket)
                        .append(field!!.column)
                        .append(op)
                        .append(" ? ")

                    openBracket = StringBuilder()
                    closeBracket = StringBuilder()
                    conj = " and "
                    not = false
                }

            }
        }

        builder.append(";")

        return builder.toString()
    }

    fun getRelationship(entity: EntityMeta, field: FieldMeta): String {
        val relationship = field.relationshipEntity?.let { EntityMeta(it) }
        if(relationship !is EntityMeta) {
            throw Exception("Could not find relationship for field.")
        }

        val entityColumn = StringBuilder(entity.table).append("_").append(entity.primaryKey.column).toString()
        val relationshipColumn = StringBuilder(relationship.table).append("_").append(relationship.primaryKey.column).toString()

        val builder = StringBuilder("select ")
            .append(relationship.table).append(".* from ")
            .append(field.assignmentTable)
            .append(" left join ")
            .append(relationship.table)
            .append(" on ")
            .append(field.assignmentTable).append(".").append(relationshipColumn)
            .append(" = ")
            .append(relationship.table).append(".").append(relationship.primaryKey.column)
            .append(" where ").append(field.assignmentTable).append(".").append(entityColumn)
            .append(" = ?;")

        return builder.toString()
    }

}
