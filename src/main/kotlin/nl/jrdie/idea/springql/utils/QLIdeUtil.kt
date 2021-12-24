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

package nl.jrdie.idea.springql.utils

import com.intellij.psi.PsiElement
import nl.jrdie.idea.springql.models.annotations.SchemaMappingType
import org.jetbrains.uast.*

object QLIdeUtil {

    fun getGraphQlField(uMethod: UMethod): String? {
        val annotationInfo = getSchemaMappingAnnotationInfo(uMethod).firstOrNull()
        if (annotationInfo == null) {
            return null
        }

        val field: String? = when (annotationInfo.mappingType) {
            SchemaMappingType.SCHEMA_MAPPING -> annotationInfo.uAnnotation.getValue("field")
                ?: annotationInfo.uAnnotation.getValue("value")
            SchemaMappingType.MUTATION_MAPPING,
            SchemaMappingType.QUERY_MAPPING,
            SchemaMappingType.SUBSCRIPTION_MAPPING -> annotationInfo.uAnnotation.getValue("name")
                ?: annotationInfo.uAnnotation.getValue("value")
        }

        if (field != null) {
            return field
        }

        return uMethod.name
    }

    fun UAnnotation.getValue(attribute: String): String? {
        return this
            .findAttributeValue(attribute)
            ?.evaluateString()
    }

    fun getSchemaMappingTypeName(uMethod: UMethod): String? {
        val annotationInfo = getSchemaMappingAnnotationInfo(uMethod).firstOrNull()
        if (annotationInfo == null) {
            // Handle case where @SchemaMapping(typeName = "MyType") annotation is present on the class.
            val classSchemaMappingName: String? = uMethod
                .getContainingUClass()
                ?.findAnnotation(SchemaMappingType.SCHEMA_MAPPING.qualifiedAnnotationName)
                ?.getValue("typeName")

            if (classSchemaMappingName != null) {
                return classSchemaMappingName
            }
            return null
        }

        return when (annotationInfo.mappingType) {
            SchemaMappingType.QUERY_MAPPING -> "Query"
            SchemaMappingType.MUTATION_MAPPING -> "Mutation"
            SchemaMappingType.SUBSCRIPTION_MAPPING -> "Subscription"
            SchemaMappingType.SCHEMA_MAPPING -> {
                val typeNameValue = annotationInfo.uAnnotation.getValue("typeName")

                if (typeNameValue == null) {
                    // TODO
                    return "MethodTypeOfFirstParam"
                }

                return typeNameValue
            }
        }
//        val annotationField = SpringAliasForUtils.findAliasFor(
//            annotationInfo.uAnnotation.sourcePsi,
//            "org.springframework.graphql.data.method.annotation",
//            annotationInfo.mappingType.qualifiedAnnotationName,
//            "typeName",
//        )
//
//        if (annotationField?.annotation != null) {
//            return getAnnotationAttributeValue(annotationInfo.uAnnotation, annotationField.attributeName)
//        }
//
//        return null // TODO MethodParameter
    }

    fun getSchemaMappingAnnotationInfo(uMethod: UMethod): List<SchemaMappingAnnotationInfo> {
        return uMethod
            .uAnnotations
            .mapNotNull { uAnnotation ->
                SchemaMappingType
                    .getSchemaMappingType(uAnnotation.qualifiedName!!)
                    ?.let { mappingType -> SchemaMappingAnnotationInfo(uAnnotation, mappingType) }
            }
    }

    fun getSchemaMappingAnnotationInfo(uAnnotation: UAnnotation): SchemaMappingAnnotationInfo? {
        return SchemaMappingType.getSchemaMappingType(uAnnotation.qualifiedName!!)
            ?.let { SchemaMappingAnnotationInfo(uAnnotation, it) }
    }

    fun isSchemaMappingMethod(psiElement: PsiElement): Boolean {
        val uElement = psiElement.toUElement()
        if (uElement !is UMethod) {
            return false
        }
        return getSchemaMappingAnnotationInfo(uElement) != null
    }

    fun isSchemaMappingAnnotation(uAnnotation: UAnnotation): Boolean {
        return SchemaMappingType.isSchemaMappingAnnotation(uAnnotation.qualifiedName)
    }

    fun isBatchMappingAnnotation(uAnnotation: UAnnotation): Boolean {
        return uAnnotation.qualifiedName == "org.springframework.graphql.data.method.annotation.BatchMapping"
    }
}