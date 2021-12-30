package nl.jrdie.idea.springql.ide.completion

import com.intellij.openapi.components.service
import com.intellij.psi.PsiReference
import com.intellij.psi.UastReferenceProvider
import com.intellij.util.ProcessingContext
import nl.jrdie.idea.springql.references.QLFieldPolyReference
import nl.jrdie.idea.springql.svc.QLIdeService
import org.jetbrains.uast.UElement
import org.jetbrains.uast.UNamedExpression
import org.jetbrains.uast.expressions.UInjectionHost
import org.jetbrains.uast.getParentOfType

@Suppress("FoldInitializerAndIfToElvis")
class QLSchemaMappingFieldNameRefProvider : UastReferenceProvider(UInjectionHost::class.java) {

    override fun getReferencesByElement(element: UElement, context: ProcessingContext): Array<PsiReference> {
        if (element !is UInjectionHost) {
            throw IllegalStateException("Should not happen")
        }

        val sourcePsi = element.sourcePsi
        if (sourcePsi == null /* || sourcePsi.project == null */) {
            return PsiReference.EMPTY_ARRAY
        }

        val svc = sourcePsi.project.service<QLIdeService>()
        if (!svc.isApplicableProject(sourcePsi.project)) {
            return PsiReference.EMPTY_ARRAY
        }

        val attribute = element.getParentOfType<UNamedExpression>()?.name
        // TODO Mapping specific, or use Spring
        if (attribute != "value" && attribute != "name" && attribute != "field") {
            return PsiReference.EMPTY_ARRAY
        }

        return arrayOf(QLFieldPolyReference(sourcePsi))
    }
}
