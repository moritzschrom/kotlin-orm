package co.schrom.orm.postgres

import co.schrom.orm.Orm
import co.schrom.orm.OrmFactory

class PostgresOrmFactory : OrmFactory() {

    override fun createOrm(): Orm {
        return PostgresOrm()
    }

}
