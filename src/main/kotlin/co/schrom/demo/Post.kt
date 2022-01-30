package co.schrom.demo

import co.schrom.orm.annotations.*

@Entity(table = "t_post")
class Post(
    @Field
    @PrimaryKey
    val id: Int,

    @Field
    val title: String,

    @Field
    val content: String,

    @Field
    @Relationship(RelationshipType.MANY_TO_ONE, assignmentTable = "t_author_post", entity = User::class)
    val author: User,

    @Field
    @Relationship(RelationshipType.MANY_TO_MANY, assignmentTable = "t_category_post", entity = Category::class)
    val categories: List<Category>,
)
