package co.schrom.demo

class Post(
    val id: Int,
    val title: String,
    val content: String,
    val author: User,
    val category: Category,
)
