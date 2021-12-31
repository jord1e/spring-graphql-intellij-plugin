package nl.jrdie.idea.springql.types

import com.intellij.psi.PsiElement
import org.jetbrains.uast.UAnnotation
import org.jetbrains.uast.UMethod

data class SchemaMappingSummary(
    val typeName: String,
    val fieldName: String,
    val annotationPsi: PsiElement,
    val schemaPsi: PsiElement?,
    val uAnnotation: UAnnotation,
    val annotationName: String,
    val uMethod: UMethod,
    val mappingType: SchemaMappingType,
    val methodPsi: PsiElement
)
