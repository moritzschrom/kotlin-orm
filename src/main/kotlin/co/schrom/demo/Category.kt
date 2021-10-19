package co.schrom.demo

import co.schrom.orm.annotations.Entity
import co.schrom.orm.annotations.Field
import co.schrom.orm.annotations.PrimaryKey

@Entity(table = "t_category")
class Category(
    @Field
    @PrimaryKey
    val id: Int,

    @Field
    val title: String,
)
