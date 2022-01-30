package co.schrom.orm

import kotlin.reflect.KClass

class QueryMeta<T>(_entity: EntityMeta, _previous: QueryMeta<T>? = null) : Iterable<T> {

    val entity = _entity

    val previous = _previous

    var operation: QueryOperation = QueryOperation.NOP

    var args: Array<*>? = null

    private var internals: ArrayList<T>? = null

    private fun setOperation(operation: QueryOperation, vararg args: Any): QueryMeta<T> {
        this.operation = operation
        this.args = args
        return QueryMeta(entity, this)
    }

    private fun getInternals(): ArrayList<T> {
        if(internals == null) internals = arrayListOf()
        return internals as ArrayList<T>
    }

    override fun iterator(): Iterator<T> {
        return getInternals().iterator()
    }

    fun getParameters(): ArrayList<Any?> {
        val operations = arrayListOf<QueryMeta<*>>()
        var parameters = arrayListOf<Any?>()

        var q: QueryMeta<*>? = this
        while(q != null) {
            operations.add(0, q)
            q = q.previous
        }

        for(operation in operations) {
            when(operation.operation) {

                QueryOperation.EQUALS, QueryOperation.LIKE -> {
                    parameters.add(operation.args!![1])
                }

                QueryOperation.IN -> {
                    for(i in 1 until operation.args!!.size) {
                       parameters.add(operation.args!![i])
                    }
                }

                QueryOperation.GT, QueryOperation.LT -> {
                    parameters.add(operation.args!![1])
                }
            }
        }

        return parameters
    }

    // Operator methods

    fun not(): QueryMeta<T> {
        return setOperation(QueryOperation.NOT)
    }

    fun and(): QueryMeta<T> {
        return setOperation(QueryOperation.AND)
    }

    fun or(): QueryMeta<T> {
        return setOperation(QueryOperation.OR)
    }

    fun group(): QueryMeta<T> {
        return setOperation(QueryOperation.GROUP)
    }

    fun endGroup(): QueryMeta<T> {
        return setOperation(QueryOperation.END_GROUP)
    }

    fun equals(field: String, value: Any, ignoreCase: Boolean): QueryMeta<T> {
        return setOperation(QueryOperation.EQUALS, field, value, ignoreCase)
    }

    fun equals(field: String, value: Any): QueryMeta<T> {
        return equals(field, value, false)
    }

    fun like(field: String, value: Any, ignoreCase: Boolean): QueryMeta<T> {
        return setOperation(QueryOperation.LIKE, field, value, ignoreCase)
    }

    fun like(field: String, value: Any): QueryMeta<T> {
        return setOperation(QueryOperation.LIKE, field, value, false)
    }

    fun contains(field: String, vararg values: Any): QueryMeta<T> {
        return setOperation(QueryOperation.IN, field, values)
    }

    fun greaterThan(field: String, value: Any): QueryMeta<T> {
        return setOperation(QueryOperation.GT, field, value)
    }

    fun lessThan(field: String, value: Any): QueryMeta<T> {
        return setOperation(QueryOperation.LT, field, value)
    }
}
