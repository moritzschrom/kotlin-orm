package co.schrom.orm

import co.schrom.orm.annotations.Field
import co.schrom.orm.annotations.PrimaryKey
import kotlin.reflect.KProperty1
import kotlin.reflect.KType
import kotlin.reflect.full.hasAnnotation

class FieldMeta(kProperty: KProperty1<*, *>) {

    val column: String

    val property: KProperty1<*, *>

    val type: KType

    val length: Int

    val isPrimaryKey: Boolean

    val isNullable: Boolean

    init {
        // Retrieve the Field annotation from the parameter property
        val fieldAnnotation = kProperty.annotations.find { it is Field } as? Field

        if(fieldAnnotation !is Field) throw Exception("Could not create FieldMeta. The property does not contain the Field annotation.")

        // Save the property
        property = kProperty

        // Set the column name property
        column = if(fieldAnnotation.column == "") {
            kProperty.name.lowercase()
        } else {
            fieldAnnotation.column
        }

        // Set the type property
        type = kProperty.returnType

        // Set the length property
        length = fieldAnnotation.length

        // Set the isNullable property
        isNullable = fieldAnnotation.nullable

        // Set the isPrimaryKey property
        isPrimaryKey = kProperty.hasAnnotation<PrimaryKey>()
    }
}
