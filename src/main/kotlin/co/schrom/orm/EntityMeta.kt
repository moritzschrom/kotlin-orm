package co.schrom.orm

import co.schrom.orm.annotations.Entity
import co.schrom.orm.annotations.Field
import co.schrom.orm.annotations.Ignore
import kotlin.reflect.KClass
import kotlin.reflect.full.hasAnnotation
import kotlin.reflect.full.memberProperties

class EntityMeta(kClass: KClass<*>) {

    val table: String

    val internals = arrayListOf<FieldMeta>()

    val externals = arrayListOf<FieldMeta>()

    val primaryKey: FieldMeta

    init {
        // Retrieve the Entity annotation from the class
        val entityAnnotation: Entity? = kClass.annotations.find { it is Entity } as? Entity

        if(entityAnnotation !is Entity) throw Exception("Could not create EntityMeta. The class does not contain the Entity annotation.")

        // Set the table name
        table = if(entityAnnotation.table == "") {
            kClass.simpleName?.lowercase().toString()
        } else {
            entityAnnotation.table
        }

        // Retrieve the Fields from the class
        var cachedPrimaryKey: FieldMeta? = null
        kClass.memberProperties.forEach {
            // Guard clauses
            // Ignore fields without the Field annotation
            // Ignore fields with the Ignore annotation
            if(!it.hasAnnotation<Field>() || it.hasAnnotation<Ignore>()) return@forEach

            // Create a FieldMeta object
            val fieldMeta = FieldMeta(it)

            // Update the cached primary key if necessary with the current FieldMeta instance
            if(fieldMeta.isPrimaryKey) cachedPrimaryKey = fieldMeta

            // Add the current FieldMeta instance to the fields ArrayList
            if(fieldMeta.isExternal) {
                externals.add(fieldMeta)
            } else {
                internals.add(fieldMeta)
            }
        }

        // If there is no primary key present, throw an exception
        if(cachedPrimaryKey == null) {
            throw Exception("Could not create EntityMeta. The class does not contain a PrimaryKey.")
        }
        primaryKey = cachedPrimaryKey as FieldMeta
    }

}
