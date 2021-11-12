/*
 * Copyright (C) 2021 Jordie
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

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
