package nl.jrdie.idea.springql.index.entry

import com.intellij.psi.PsiElement

data class QLMethodBatchMappingIndexEntry(
    val parentType: String,
    val field: String,
    val annotationPsi: PsiElement,
    val methodPsi: PsiElement,
    /**
     * @see
     */
    val conformsToReturnSpec: Boolean,
    val schemaPsi: List<PsiElement>
) {
}