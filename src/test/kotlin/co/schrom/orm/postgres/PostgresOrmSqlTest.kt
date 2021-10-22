package co.schrom.orm.postgres

import co.schrom.orm.EntityMeta
import co.schrom.orm.FieldMeta
import io.mockk.every
import io.mockk.mockk
import kotlin.reflect.full.createType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails

class PostgresOrmSqlTest {

    @Test
    fun typeDefinition_Integer_CorrectTypeDefinitionReturned() {
        // Arrange
        val field = mockk<FieldMeta>()
        every { field.type } returns Int::class.createType()

        // Act
        val typeDefinition = PostgresOrmSql.typeDefinition(field)

        // Assert
        assertEquals("integer", typeDefinition)
    }

    @Test
    fun typeDefinition_UnsupportedType_ExceptionThrown() {
        // Arrange
        class UnsupportedType
        val field = mockk<FieldMeta>()
        every { field.type } returns UnsupportedType::class.createType()

        // Assert
        assertFails {
            // Act
            PostgresOrmSql.typeDefinition(field)
        }
    }

    @Test
    fun dropTableIfExists_SimpleEntity_CorrectSqlReturned() {
        // Arrange
        val entity = mockk<EntityMeta>()
        every { entity.table } returns "t_table"

        // Act
        val sql = PostgresOrmSql.dropTableIfExists(entity)

        // Assert
        assertEquals("drop table if exists t_table cascade;", sql)
    }

    @Test
    fun createTable_SimpleEntity_CorrectSqlReturned() {
        // Arrange
        val entity = mockk<EntityMeta>()
        every { entity.table } returns "t_table"
        every { entity.fields } returns arrayListOf(
            mockk {
                every { column } returns "id"
                every { type } returns Int::class.createType()
                every { isPrimaryKey } returns true
                every { isNullable } returns false
            },
            mockk {
                every { column } returns "name"
                every { type } returns String::class.createType()
                every { length } returns 255
                every { isPrimaryKey } returns false
                every { isNullable } returns false
            }
        )

        // Act
        val sql = PostgresOrmSql.createTable(entity)

        // Assert
        assertEquals("create table t_table( id integer not null primary key , name varchar(255) not null );", sql)
    }

    @Test
    fun createTable_EntityMetaWithNullableColumn_CorrectSqlReturned() {
        // Arrange
        val entity = mockk<EntityMeta>()
        every { entity.table } returns "t_table"
        every { entity.fields } returns arrayListOf(
            mockk {
                every { column } returns "id"
                every { type } returns Int::class.createType()
                every { isPrimaryKey } returns true
                every { isNullable } returns false
            },
            mockk {
                every { column } returns "name"
                every { type } returns String::class.createType()
                every { length } returns 255
                every { isPrimaryKey } returns false
                every { isNullable } returns true
            }
        )

        // Act
        val sql = PostgresOrmSql.createTable(entity)

        // Assert
        assertEquals("create table t_table( id integer not null primary key , name varchar(255) );", sql)
    }

    @Test
    fun insert_SimpleEntity_CorrectSqlReturned() {
        // Arrange
        val entity = mockk<EntityMeta>()
        every { entity.table } returns "t_table"
        every { entity.fields } returns arrayListOf(
            mockk {
                every { column } returns "id"
                every { type } returns Int::class.createType()
                every { isPrimaryKey } returns true
                every { isNullable } returns false
            },
            mockk {
                every { column } returns "title"
                every { type } returns String::class.createType()
                every { length } returns 255
                every { isPrimaryKey } returns false
                every { isNullable } returns false
            }
        )

        // Act
        val sql = PostgresOrmSql.insert(entity)

        // Assert
        assertEquals("insert into t_table( id , title ) values( ? , ? );", sql)
    }

    @Test
    fun delete_SimpleEntity_CorrectSqlReturned() {
        // Arrange
        val entity = mockk<EntityMeta>()
        every { entity.table } returns "t_table"
        every { entity.primaryKey } returns mockk {
            every { column } returns "pk_id"
            every { type } returns Int::class.createType()
            every { isPrimaryKey } returns true
            every { isNullable } returns false
        }

        // Act
        val sql = PostgresOrmSql.delete(entity)

        // Assert
        assertEquals("delete from t_table where pk_id = ?;", sql)
    }
}
