package cz.encircled.joiner.ksp

val basicTypeToPathType: Map<String, String> = mapOf(
    "java.lang.String" to "StringPath",
    "kotlin.String" to "StringPath",
    "String" to "StringPath",

    "java.lang.Integer" to "NumberPath<Integer>",
    "Integer" to "NumberPath<Integer>",
    "int" to "NumberPath<Integer>",
    "kotlin.Int" to "NumberPath<Integer>",

    "java.lang.Long" to "NumberPath<Long>",
    "Long" to "NumberPath<Long>",
    "long" to "NumberPath<Long>",
    "kotlin.Long" to "NumberPath<Long>",

    "java.lang.Boolean" to "BooleanPath",
    "boolean" to "BooleanPath",
    "Boolean" to "BooleanPath",
    "kotlin.Boolean" to "BooleanPath",

    "java.lang.Float" to "NumberPath<Float>",
    "Float" to "NumberPath<Float>",
    "float" to "NumberPath<Float>",
    "kotlin.Float" to "NumberPath<Float>",

    "java.lang.Double" to "NumberPath<Double>",
    "Double" to "NumberPath<Double>",
    "double" to "NumberPath<Double>",
    "kotlin.Double" to "NumberPath<Double>",

    "java.lang.Short" to "NumberPath<Short>",
    "Short" to "NumberPath<Short>",
    "short" to "NumberPath<Short>",
    "kotlin.Short" to "NumberPath<Short>",

    "java.lang.Byte" to "NumberPath<Byte>",
    "Byte" to "NumberPath<Byte>",
    "byte" to "NumberPath<Byte>",
    "kotlin.Byte" to "NumberPath<Byte>",

    "java.util.Date" to "DatePath<java.util.Date>",
    "java.time.LocalDate" to "DatePath<java.time.LocalDate>",
    "java.time.Instant" to "DateTimePath<java.time.Instant>",
    "java.time.LocalDateTime" to "DateTimePath<java.time.LocalDateTime>",

    "java.math.BigDecimal" to "NumberPath<java.math.BigDecimal>",
    "java.math.BigInteger" to "NumberPath<java.math.BigInteger>"
)

val arrayTypeMapping: Map<String, String> = mapOf(
    "kotlin.Array" to "Array",
    "kotlin.ByteArray" to "Array",
)

val mapTypeMapping: Map<String, String> = mapOf(
    "java.util.Map" to "Map",
    "kotlin.collections.Map" to "Map",
    "kotlin.collections.Mutable.Map" to "Map",
)

val listTypeMapping: Map<String, String> = mapOf(
    "java.util.List" to "List",
    "kotlin.collections.List" to "List",
    "kotlin.collections.MutableList" to "List",
)

val setTypeMapping: Map<String, String> = mapOf(
    "java.util.Set" to "Set",
    "kotlin.collections.Set" to "Set",
    "kotlin.collections.MutableSet" to "Set",
)

val collectionTypeMapping: Map<String, String> = mapOf(
    "java.util.Collection" to "Collection",
    "kotlin.collections.Collection" to "Collection",
    "kotlin.Array" to "Collection"
) + setTypeMapping + listTypeMapping + arrayTypeMapping + mapTypeMapping