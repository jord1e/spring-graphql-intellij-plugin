package nl.jrdie.idea.springql.index

import com.intellij.psi.PsiElement
import org.jetbrains.uast.UAnnotation
import org.jetbrains.uast.UMethod

data class SchemaMappingAnnotationIndexEntry(
    val parentType: String,
    val field: String,
    val dataFetcherMethod: UMethod,
    val annotation: UAnnotation,
    val graphQlSchemaPsi: PsiElement?
)

