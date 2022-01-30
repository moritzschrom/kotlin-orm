package co.schrom.orm

enum class QueryOperation {
    NOP,
    NOT,
    AND,
    OR,
    GROUP,
    END_GROUP,
    EQUALS,
    LIKE,
    IN,
    GT,
    LT
}
