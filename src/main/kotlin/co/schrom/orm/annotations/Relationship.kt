package co.schrom.orm.annotations

import kotlin.reflect.KClass

@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.RUNTIME)
annotation class Relationship(
    val relationshipType: RelationshipType,
    val assignmentTable: String,
    val entity: KClass<*>,
)
