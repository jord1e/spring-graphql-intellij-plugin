package nl.jrdie.idea.springql.ide.completion

import com.intellij.psi.PsiReference
import com.intellij.psi.UastReferenceProvider
import com.intellij.util.ProcessingContext
import nl.jrdie.idea.springql.references.QLTypeNamePolyReference
import org.jetbrains.uast.UElement
import org.jetbrains.uast.UNamedExpression
import org.jetbrains.uast.expressions.UInjectionHost
import org.jetbrains.uast.getParentOfType

class QLSchemaMappingTypeNameRefProvider : UastReferenceProvider(UInjectionHost::class.java) {

    override fun getReferencesByElement(element: UElement, context: ProcessingContext): Array<PsiReference> {
        if (element !is UInjectionHost) {
            throw IllegalStateException("Should not happen")
        }

        if (element.sourcePsi?.project == null) {
            return PsiReference.EMPTY_ARRAY
        }

        val attribute = element.getParentOfType<UNamedExpression>()?.name
        if (attribute != "typeName") {
            return PsiReference.EMPTY_ARRAY
        }

        return arrayOf(QLTypeNamePolyReference(element.sourcePsi!!))
    }
}
