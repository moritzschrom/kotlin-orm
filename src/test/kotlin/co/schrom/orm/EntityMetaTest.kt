package co.schrom.orm

import co.schrom.orm.annotations.Entity
import co.schrom.orm.annotations.Field
import co.schrom.orm.annotations.Ignore
import co.schrom.orm.annotations.PrimaryKey
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails

class EntityMetaTest {

    @Test
    fun init_FullyAnnotated_InitiatedWithAnnotatedValues() {
        // Arrange
        @Entity(table="t_test")
        class TestClass(
            @Field
            @PrimaryKey
            val id: Int,
        )

        // Act
        val entityMeta = EntityMeta(TestClass::class)

        // Assert
        assertEquals("t_test", entityMeta.table)
        assertEquals(1, entityMeta.internals.size)
    }

    @Test
    fun init_LazyAnnotated_InitiatedWithDefaultValues() {
        // Arrange
        @Entity
        class TestClass(
            @Field
            @PrimaryKey
            val id: Int,
        )

        // Act
        val entityMeta = EntityMeta(TestClass::class)

        // Assert
        assertEquals("testclass", entityMeta.table)
        assertEquals(1, entityMeta.internals.size)
    }

    @Test
    fun init_IgnoreField_InitiatedWithoutIgnoredField() {
        // Arrange
        @Entity
        class TestClass(
            @Field
            @PrimaryKey
            val id: Int,

            @Ignore
            val ignore: String,
        )

        // Act
        val entityMeta = EntityMeta(TestClass::class)

        // Assert
        assertEquals(1, entityMeta.internals.size)
    }

    @Test
    fun init_NoEntityAnnotation_Exception_Thrown() {
        // Arrange
        class TestClass(
            @Field
            @PrimaryKey
            val id: Int,
        )

        // Assert
        assertFails {
            // Act
            EntityMeta(TestClass::class)
        }
    }

    @Test
    fun init_NoPrimaryKeyAnnotation_Exception_Thrown() {
        // Arrange
        @Entity
        class TestClass(
            @Field
            val id: Int,
        )

        // Assert
        assertFails {
            // Act
            EntityMeta(TestClass::class)
        }
    }

}
