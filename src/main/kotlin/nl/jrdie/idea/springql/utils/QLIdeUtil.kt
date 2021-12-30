package nl.jrdie.idea.springql.utils

import com.intellij.lang.jsgraphql.types.language.Type
import com.intellij.lang.jsgraphql.types.schema.idl.TypeUtil
import org.jetbrains.kotlin.util.prefixIfNot
import org.jetbrains.uast.UAnnotation
import org.jetbrains.uast.UClass
import org.jetbrains.uast.UMethod

object QLIdeUtil {

    const val SCHEMA_MAPPING_FQN = "org.springframework.graphql.data.method.annotation.SchemaMapping"
    const val QUERY_MAPPING_FQN = "org.springframework.graphql.data.method.annotation.QueryMapping"
    const val MUTATION_MAPPING_FQN = "org.springframework.graphql.data.method.annotation.MutationMapping"
    const val SUBSCRIPTION_MAPPING_FQN = "org.springframework.graphql.data.method.annotation.SubscriptionMapping"
    const val BATCH_MAPPING_FQN = "org.springframework.graphql.data.method.annotation.BatchMapping"
    const val ARGUMENT_FQN = "org.springframework.graphql.data.method.annotation.Argument"

    fun getGraphQlField(uAnnotation: UAnnotation): String? {
        val field: String? = when (uAnnotation.qualifiedName) {
            SCHEMA_MAPPING_FQN -> AliasForUtil.findValue(uAnnotation, "field")
                ?: AliasForUtil.findValue(uAnnotation, "value")
            QUERY_MAPPING_FQN,
            MUTATION_MAPPING_FQN,
            SUBSCRIPTION_MAPPING_FQN -> AliasForUtil.findValue(uAnnotation, "name")
                ?: AliasForUtil.findValue(uAnnotation, "value")
            else -> null
        }

        if (field != null) {
            return field
        }

        return "" // todo
    }

    fun getSchemaMappingTypeName(uMethod: UMethod): String? {
        return uMethod.uAnnotations.mapNotNull { getSchemaMappingTypeName(it) }.firstOrNull()
    }

    fun getSchemaMappingTypeName(uMethod: UClass): String? {
        return uMethod.uAnnotations.mapNotNull { getSchemaMappingTypeName(it) }.firstOrNull()
    }

    fun getGraphQlField(uMethod: UMethod): String? {
        return uMethod.uAnnotations.mapNotNull { getGraphQlField(it) }.firstOrNull()
    }

    fun getGraphQlField(uMethod: UClass): String? {
        return uMethod.uAnnotations.mapNotNull { getGraphQlField(it) }.firstOrNull()
    }

    fun getSchemaMappingTypeName(uAnnotation: UAnnotation): String? {
        return when (uAnnotation.qualifiedName) {
            QUERY_MAPPING_FQN -> "Query"
            MUTATION_MAPPING_FQN -> "Mutation"
            SUBSCRIPTION_MAPPING_FQN -> "Subscription"
            SCHEMA_MAPPING_FQN -> AliasForUtil.findValue(uAnnotation, "typeName")
            else -> null
        }
    }

    fun isBatchMappingAnnotation(uAnnotation: UAnnotation): Boolean {
        return uAnnotation.qualifiedName == "org.springframework.graphql.data.method.annotation.BatchMapping"
    }

    fun isDefaultSchemaMappingAnnotation(uAnnotation: UAnnotation): Boolean {
        return when (uAnnotation.qualifiedName) {
            "org.springframework.graphql.data.method.annotation.SchemaMapping",
            "org.springframework.graphql.data.method.annotation.QueryMapping",
            "org.springframework.graphql.data.method.annotation.SubscriptionMapping",
            "org.springframework.graphql.data.method.annotation.MutationMapping" -> true
            else -> false
        }
    }

    fun reduceSchemaMappingAnnotationName(uAnnotation: UAnnotation): String {
        return when (uAnnotation.qualifiedName) {
            "org.springframework.graphql.data.method.annotation.SchemaMapping" -> "@SchemaMapping"
            "org.springframework.graphql.data.method.annotation.QueryMapping" -> "@QueryMapping"
            "org.springframework.graphql.data.method.annotation.SubscriptionMapping" -> "@SubscriptionMapping"
            "org.springframework.graphql.data.method.annotation.MutationMapping" -> "@MutationMapping"
            "org.springframework.graphql.data.method.annotation.BatchMapping" -> "@BatchMapping"
            else -> uAnnotation.qualifiedName?.split('.')?.dropWhile { it.first().isLowerCase() }
                ?.joinToString(".")?.prefixIfNot("@")
                ?: throw IllegalArgumentException("UAnnotation does not have a FQN: $uAnnotation")
        }
    }

    @JvmStatic
    fun Type<*>.printTypeVal(): String {
        return TypeUtil.simplePrint(this)
    }
}
