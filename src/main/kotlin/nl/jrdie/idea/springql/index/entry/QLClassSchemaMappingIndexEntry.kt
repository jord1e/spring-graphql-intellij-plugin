package nl.jrdie.idea.springql.index.entry

import com.intellij.psi.PsiElement

data class QLClassSchemaMappingIndexEntry(
    val parentType: String,
    val field: String,
    val annotationPsi: PsiElement,
    val classPsi: PsiElement,
    val schemaPsi: List<PsiElement>
)
