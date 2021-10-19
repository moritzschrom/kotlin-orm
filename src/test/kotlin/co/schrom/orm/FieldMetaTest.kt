package co.schrom.orm

import co.schrom.orm.annotations.Field
import co.schrom.orm.annotations.PrimaryKey
import kotlin.reflect.full.createType
import kotlin.reflect.full.memberProperties
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails

class FieldMetaTest {

    @Test
    fun init_FullyAnnotated_InitiatedWithAnnotatedValues() {
        // Arrange
        class TestClass(
            @Field(column = "id_column", length = 1024, nullable = true)
            @PrimaryKey
            val id: String,
        )
        val property = TestClass::class.memberProperties.find { it.name == "id" }

        // Act
        val fieldMeta = FieldMeta(property!!)

        // Assert
        assertEquals("id_column", fieldMeta.column)
        assertEquals(String::class.createType(), fieldMeta.type)
        assertEquals(1024, fieldMeta.length)
        assertEquals(true, fieldMeta.isNullable)
        assertEquals(true, fieldMeta.isPrimaryKey)
    }

    @Test
    fun init_LazyAnnotated_InitiatedWithDefaultValues() {
        // Arrange
        class TestClass(
            @Field
            val property: Int,
        )
        val property = TestClass::class.memberProperties.find { it.name == "property" }

        // Act
        val fieldMeta = FieldMeta(property!!)

        // Assert
        assertEquals("property", fieldMeta.column)
        assertEquals(Int::class.createType(), fieldMeta.type)
        assertEquals(false, fieldMeta.isNullable)
        assertEquals(false, fieldMeta.isPrimaryKey)
    }

    @Test
    fun init_NoFieldAnnotation_ExceptionThrown() {
        // Arrange
        class TestClass(
            val property: String,
        )
        val property = TestClass::class.memberProperties.find { it.name == "property" }

        // Assert
        assertFails {
            // Act
            FieldMeta(property!!)
        }
    }

}
