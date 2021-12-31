package nl.jrdie.idea.springql.index.entry

import com.intellij.psi.PsiElement
import org.jetbrains.uast.UAnnotation
import org.jetbrains.uast.UMethod

data class QLMethodBatchMappingIndexEntry(
    override val parentType: String?,
    override val field: String?,
    override val annotationPsi: PsiElement,
    val methodPsi: PsiElement,
    /**
     * @see
     */
    val conformsToReturnSpec: Boolean,
    override val schemaPsi: List<PsiElement>,
    override val uAnnotation: UAnnotation,
    val uMethod: UMethod
) : SchemaMappingIndexEntry
