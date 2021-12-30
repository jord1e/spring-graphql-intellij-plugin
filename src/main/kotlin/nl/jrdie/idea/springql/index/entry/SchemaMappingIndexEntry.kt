package nl.jrdie.idea.springql.index.entry

import com.intellij.psi.PsiElement
import org.jetbrains.uast.UAnnotation

sealed interface SchemaMappingIndexEntry {

    val parentType: String?

    val field: String?

    val annotationPsi: PsiElement

    val uAnnotation: UAnnotation

    val schemaPsi: List<PsiElement>
}
