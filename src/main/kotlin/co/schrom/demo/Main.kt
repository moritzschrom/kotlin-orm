import co.schrom.demo.Category
import co.schrom.demo.Post
import co.schrom.demo.User
import co.schrom.orm.postgres.PostgresOrmFactory

fun main(args: Array<String>) {
    // Example data
    val newsCategory = Category(1, "News")
    val eventsCategory = Category(1, "Events")
    val author = User(1, "Alex Doe", "alex.doe@example.org")
    val authorToBeDeleted = User(2, "John Doe", "john.doe@example.org")
    val newsPost = Post(1, "Some news", "Lorem ipsum dolor sit amet, consetetur sadipscing elitr.", author, newsCategory)
    val eventsPost = Post(1, "Some event", "Lorem ipsum dolor sit amet, consetetur sadipscing elitr.", author, eventsCategory)

    val orm = PostgresOrmFactory().createOrm("jdbc:postgresql://localhost:5432/orm", "postgres", "postgres")

    // Cleanup
    orm.dropTableIfExists(User::class)
    orm.dropTableIfExists(Category::class)
    orm.dropTableIfExists(Post::class)

    // Create tables
    orm.createTable(User::class)
    orm.createTable(Category::class)
    orm.createTable(Post::class)

    // Create model in database
    orm.create(author)
    orm.create(authorToBeDeleted)

    // Delete model in database
    orm.delete(authorToBeDeleted)
}
