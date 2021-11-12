package nl.jrdie.idea.springql.models.annotations

enum class SchemaMappingType(val simpleName: String, val qualifiedAnnotationName: String) {

    SCHEMA_MAPPING(
        "SchemaMapping",
        "org.springframework.graphql.data.method.annotation.SchemaMapping"
    ),
    QUERY_MAPPING(
        "QueryMapping",
        "org.springframework.graphql.data.method.annotation.QueryMapping"
    ),
    MUTATION_MAPPING(
        "MutationMapping",
        "org.springframework.graphql.data.method.annotation.MutationMapping"
    ),
    SUBSCRIPTION_MAPPING(
        "SubscriptionMapping",
        "org.springframework.graphql.data.method.annotation.SubscriptionMapping"
    );

    companion object {
        private val qualifiedAnnotationMappingCache: Map<String, SchemaMappingType> = values()
            .associateBy { it.qualifiedAnnotationName }

        fun isSchemaMappingAnnotation(qualifiedAnnotationName: String?): Boolean {
            if (qualifiedAnnotationName == null) {
                return false
            }
            return qualifiedAnnotationMappingCache.containsKey(qualifiedAnnotationName)
        }

        fun getSchemaMappingType(qualifiedAnnotationName: String): SchemaMappingType? {
            return qualifiedAnnotationMappingCache[qualifiedAnnotationName]
        }
    }
}
