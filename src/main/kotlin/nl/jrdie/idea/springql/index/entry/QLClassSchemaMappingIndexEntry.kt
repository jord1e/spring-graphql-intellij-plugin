package nl.jrdie.idea.springql.index.entry

import com.intellij.psi.PsiElement
import org.jetbrains.uast.UAnnotation

data class QLClassSchemaMappingIndexEntry(
    val classPsi: PsiElement,
    override val parentType: String?,
    override val field: String?,
    override val annotationPsi: PsiElement,
    override val schemaPsi: List<PsiElement>,
    override val uAnnotation: UAnnotation,
) : SchemaMappingIndexEntry, QLIndexEntry
