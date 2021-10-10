import co.schrom.demo.Category
import co.schrom.demo.Post
import co.schrom.demo.User

fun main(args: Array<String>) {
    // Example data
    val newsCategory = Category(1, "News")
    val eventsCategory = Category(1, "Events")
    val author = User(1, "Alex Doe", "alex.doe@example.org")
    val newsPost = Post(1, "Some news", "Lorem ipsum dolor sit amet, consetetur sadipscing elitr.", author, newsCategory)
    val eventsPost = Post(1, "Some event", "Lorem ipsum dolor sit amet, consetetur sadipscing elitr.", author, eventsCategory)
}
