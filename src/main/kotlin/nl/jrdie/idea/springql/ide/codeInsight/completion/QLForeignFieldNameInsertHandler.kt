package nl.jrdie.idea.springql.ide.codeInsight.completion

import com.intellij.codeInsight.completion.InsertHandler
import com.intellij.codeInsight.completion.InsertionContext
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.lang.jsgraphql.psi.GraphQLFieldDefinition
import com.intellij.lang.jsgraphql.types.language.FieldDefinition
import com.intellij.openapi.components.service
import nl.jrdie.idea.springql.svc.QLIdeService
import nl.jrdie.idea.springql.utils.UClassAnnotatorUtil
import org.jetbrains.uast.UMethod
import org.jetbrains.uast.getUastParentOfType

@Suppress("FoldInitializerAndIfToElvis")
class QLForeignFieldNameInsertHandler(
    private val fieldDefinition: FieldDefinition,
/*private val uAnnotation: UAnnotation*/
) : InsertHandler<LookupElement> {

    override fun handleInsert(context: InsertionContext, item: LookupElement) {
        val psiElement = item.psiElement
        if (psiElement == null) {
            return
        }

        if (psiElement !is GraphQLFieldDefinition) {
            return
        }

        val svc = psiElement.project.service<QLIdeService>()
        if (!svc.isApplicableProject(psiElement.project)) {
            return
        }

        val uMethod = context.file.findElementAt(context.startOffset).getUastParentOfType<UMethod>()
        if (uMethod == null) {
            return
        }

        val summary = svc.getSummaryForMethod(uMethod)
        if (summary == null) {
            // Not a method mapping
            // TODO Spring meta-annotation support
            return
        }

        val parentTypeDefinition = svc.schemaRegistry.getParentType(fieldDefinition)
        if (parentTypeDefinition == null) {
            return
        }

        UClassAnnotatorUtil.setAnnotationParameterStringValue(
            summary.annotationPsi,
            "typeName",
            parentTypeDefinition.name!!
        )
    }
}
