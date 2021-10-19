package co.schrom.demo

import co.schrom.orm.annotations.Entity
import co.schrom.orm.annotations.Field
import co.schrom.orm.annotations.PrimaryKey

@Entity(table = "t_post")
class Post(
    @Field
    @PrimaryKey
    val id: Int,

    @Field
    val title: String,

    @Field
    val content: String,

    val author: User,

    val category: Category,
)
