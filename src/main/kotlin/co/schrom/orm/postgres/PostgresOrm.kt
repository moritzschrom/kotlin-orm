package co.schrom.orm.postgres

import co.schrom.orm.Orm
import java.sql.Connection

class PostgresOrm(override val connection: Connection) : Orm {

}
