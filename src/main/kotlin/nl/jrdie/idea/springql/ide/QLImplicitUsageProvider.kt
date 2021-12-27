package nl.jrdie.idea.springql.ide

import com.intellij.codeInsight.daemon.ImplicitUsageProvider
import com.intellij.openapi.components.service
import com.intellij.psi.PsiElement
import nl.jrdie.idea.springql.svc.QLIdeService
import org.jetbrains.uast.UMethod
import org.jetbrains.uast.toUElementOfType

/**
 * Disables the <pre>Method 'methodName()' is never used</pre> hint.
 * @see QLIdeService.isMethodUsed
 */
class QLImplicitUsageProvider : ImplicitUsageProvider {

    override fun isImplicitUsage(element: PsiElement): Boolean {
        val svc = element.project.service<QLIdeService>()
        if (!svc.isApplicableProject(element.project)) {
            return false
        }

        val uElement = element.toUElementOfType<UMethod>()
        if (uElement is UMethod) {
            return svc.isMethodUsed(uElement)
        }

        return false
    }

    override fun isImplicitRead(element: PsiElement) = false

    override fun isImplicitWrite(element: PsiElement) = false
}
