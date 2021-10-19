package co.schrom.orm.annotations

@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.RUNTIME)
annotation class Field(
    val column: String = "",
    val length: Int = 255,
    val nullable: Boolean = false,
)
