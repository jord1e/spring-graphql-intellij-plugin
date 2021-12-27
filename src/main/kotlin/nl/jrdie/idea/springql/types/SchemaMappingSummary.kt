package nl.jrdie.idea.springql.types

import com.intellij.psi.PsiElement
import org.jetbrains.uast.UAnnotation

data class SchemaMappingSummary(
    val typeName: String,
    val fieldName: String,
    val annotationPsi: PsiElement,
    val schemaPsi: PsiElement?,
    val uAnnotation: UAnnotation,
    val annotationName: String
)
