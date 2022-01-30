import co.schrom.demo.Category
import co.schrom.demo.Post
import co.schrom.demo.User
import co.schrom.orm.postgres.PostgresOrmFactory

fun main(args: Array<String>) {
    // Example data
    val newsCategory = Category(1, "News")
    val eventsCategory = Category(2, "Events")
    val sportsCategory = Category(3, "Sports")
    val author = User(1, "Alex Doe", "alex.doe@example.org")
    val authorToBeDeleted = User(2, "John Doe", "john.doe@example.org")
    val sportNewsPost = Post(1, "Some sport news", "Lorem ipsum dolor sit amet, consetetur sadipscing elitr.", author, arrayListOf(newsCategory, sportsCategory))
    val sportEventPost = Post(2, "Some sport event", "Lorem ipsum dolor sit amet, consetetur sadipscing elitr.", author, arrayListOf(eventsCategory, sportsCategory))

    val orm = PostgresOrmFactory().createOrm("jdbc:postgresql://localhost:5432/orm", "postgres", "postgres")

    // Cleanup
    orm.dropTableIfExists(User::class)
    orm.dropTableIfExists(Category::class)
    orm.dropTableIfExists(Post::class)
    orm.dropAssignmentTablesIfExist(Post::class)

    // Create tables
    orm.createTable(User::class)
    orm.createTable(Category::class)
    orm.createTable(Post::class)
    orm.createAssignmentTables(Post::class)

    // Create model in database
    orm.create(author)
    orm.create(authorToBeDeleted)
    orm.create(newsCategory)
    orm.create(eventsCategory)
    orm.create(sportsCategory)
    orm.create(sportNewsPost)
    orm.create(sportEventPost)

    // Delete model in database
    // orm.delete(authorToBeDeleted)

    // Update model in database
    val authorUpdated = User(1, "Alex Doe Updated", "alex.doe.updated@example.org")
    orm.update(authorUpdated)

    val authorGet = orm.get(User::class, 1)
    val sportNewsPostGet = orm.get(Post::class, 1)
    println(sportNewsPostGet)
    val sportEventPostGet = orm.get(Post::class, 1)
    println(sportEventPostGet)

    val query = orm.query(User::class)
        .like("name", "%dOE%", true)
        .or().like("email", "%@doe.com")
    val authorsNamedDoe = orm.get(User::class, query)
}
