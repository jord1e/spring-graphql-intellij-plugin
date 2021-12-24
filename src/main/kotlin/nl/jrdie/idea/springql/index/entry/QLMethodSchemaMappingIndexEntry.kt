package nl.jrdie.idea.springql.index.entry

import com.intellij.psi.PsiElement

data class QLMethodSchemaMappingIndexEntry(
    val parentType: String,
    val field: String,
    val annotationPsi: PsiElement,
    val methodPsi: PsiElement,
    val schemaPsi: List<PsiElement>
)
