package co.schrom.demo

import co.schrom.orm.annotations.Entity
import co.schrom.orm.annotations.Field
import co.schrom.orm.annotations.PrimaryKey

@Entity
class User(
    @Field
    @PrimaryKey
    val id: Int,

    @Field
    val name: String,

    @Field
    val email: String,
)
