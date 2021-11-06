package nl.jrdie.idea.springql.index

import com.intellij.psi.PsiElement
import nl.jrdie.idea.springql.utils.SpringGraphQlIdeUtil
import org.jetbrains.uast.UAnnotation
import org.jetbrains.uast.UMethod
import kotlin.test.assertNotNull

data class SchemaMappingAnnotationIndexEntry(val dataFetcherMethod: UMethod, val annotation: UAnnotation, val graphQlSchemaPsi: PsiElement?) {

    val parentType: String
    val field: String

    init {
        val parentType = SpringGraphQlIdeUtil.getSchemaMappingTypeName(dataFetcherMethod)
        this.parentType = assertNotNull(parentType)
        val field = SpringGraphQlIdeUtil.getGraphQlField(dataFetcherMethod)
        this.field = assertNotNull(field)
    }

}
