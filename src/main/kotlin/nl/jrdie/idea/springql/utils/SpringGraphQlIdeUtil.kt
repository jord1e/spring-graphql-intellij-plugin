package nl.jrdie.idea.springql.utils

import com.intellij.psi.PsiElement
import com.intellij.spring.model.aliasFor.SpringAliasForUtils
import nl.jrdie.idea.springql.models.annotations.SchemaMappingType
import org.jetbrains.uast.UAnnotation
import org.jetbrains.uast.UMethod
import org.jetbrains.uast.evaluateString
import org.jetbrains.uast.toUElement

object SpringGraphQlIdeUtil {

    fun getGraphQlField(uMethod: UMethod): String? {
        val annotationInfo = getSchemaMappingAnnotationInfo(uMethod)?.first()
        if (annotationInfo == null) {
            return null
        }

        val annotationField = SpringAliasForUtils.findAliasFor(
            annotationInfo.uAnnotation.sourcePsi,
            annotationInfo.mappingType.qualifiedAnnotationName,
            "org.springframework.graphql.data.method.annotation",
            "field",
        )

        if (annotationField?.annotation != null) {
            return getAnnotationAttributeValue(annotationInfo.uAnnotation, annotationField.attributeName)
        }

        return uMethod.name
    }

    fun getAnnotationAttributeValue(uAnnotation: UAnnotation, attribute: String): String? {
        return uAnnotation
            .findAttributeValue(attribute)
            ?.evaluateString()
    }

    fun getSchemaMappingTypeName(uMethod: UMethod): String? {
        val annotationInfo = getSchemaMappingAnnotationInfo(uMethod)?.first()
        if (annotationInfo == null) {
            return null // TODO MethodParameter
        }

        return when (annotationInfo.mappingType) {
            SchemaMappingType.QUERY_MAPPING -> "Query"
            SchemaMappingType.MUTATION_MAPPING -> "Mutation"
            SchemaMappingType.SUBSCRIPTION_MAPPING -> "Subscription"
            SchemaMappingType.SCHEMA_MAPPING -> {
                val typeNameValue = getAnnotationAttributeValue(annotationInfo.uAnnotation, "typeName")

                if (typeNameValue == null) {
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
}