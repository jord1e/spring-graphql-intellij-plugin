package nl.jrdie.idea.springql.ide.completion

import com.intellij.openapi.components.service
import com.intellij.psi.PsiReference
import com.intellij.psi.UastReferenceProvider
import com.intellij.util.ProcessingContext
import nl.jrdie.idea.springql.references.QLArgumentNamePolyReference
import nl.jrdie.idea.springql.svc.QLIdeService
import org.jetbrains.uast.UElement
import org.jetbrains.uast.UMethod
import org.jetbrains.uast.expressions.UInjectionHost
import org.jetbrains.uast.getUastParentOfType

@Suppress("FoldInitializerAndIfToElvis")
class QLSchemaArgumentNameRefProvider : UastReferenceProvider(UInjectionHost::class.java) {

    override fun getReferencesByElement(element: UElement, context: ProcessingContext): Array<PsiReference> {
        if (element !is UInjectionHost) {
            throw IllegalStateException("Should not happen")
        }

        val sourcePsi = element.sourcePsi
        if (sourcePsi?.project == null) {
            return PsiReference.EMPTY_ARRAY
        }

        val uMethod = sourcePsi.getUastParentOfType<UMethod>()
        if (uMethod !is UMethod) {
            return PsiReference.EMPTY_ARRAY
        }

        val svc = sourcePsi.project.service<QLIdeService>()
        val summary = svc.getSummaryForMethod(uMethod)
        if (summary == null) {
            return PsiReference.EMPTY_ARRAY
        }

        return arrayOf(QLArgumentNamePolyReference(sourcePsi))
    }
}
