package co.schrom.orm.postgres

import co.schrom.orm.Orm
import co.schrom.orm.OrmFactory
import java.sql.Connection

class PostgresOrmFactory : OrmFactory() {

    override fun createOrm(connection: Connection): Orm {
        return PostgresOrm(connection)
    }

}
