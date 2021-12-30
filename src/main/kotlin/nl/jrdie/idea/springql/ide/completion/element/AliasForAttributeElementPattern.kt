package nl.jrdie.idea.springql.ide.completion.element

import com.intellij.openapi.util.Key
import com.intellij.patterns.StandardPatterns
import com.intellij.patterns.uast.UElementPattern
import com.intellij.patterns.uast.uAnnotationQualifiedNamePattern
import com.intellij.util.ProcessingContext
import org.jetbrains.uast.expressions.UInjectionHost
import org.jetbrains.uast.getContainingUAnnotationEntry

class AliasForAttributeElementPattern(private val annotationFqn: String, private val attribute: String) :
    UElementPattern<UInjectionHost, AliasForAttributeElementPattern>(UInjectionHost::class.java) {

    private companion object {
        private val IS_UAST_ANNOTATION_PARAMETER_AF: Key<Boolean> = Key.create("IS_UAST_ANNOTATION_PARAMETER_AF")
    }

    override fun accepts(t: Any?, context: ProcessingContext?): Boolean {
        if (t !is UInjectionHost) {
            return false
        }

        val sharedContext = context?.sharedContext
        val isAnnotationParameter = sharedContext?.get(IS_UAST_ANNOTATION_PARAMETER_AF, t)
        if (isAnnotationParameter == java.lang.Boolean.FALSE) {
            return false
        }

        val containingUAnnotationEntry = getContainingUAnnotationEntry(t)
        if (containingUAnnotationEntry == null) {
            sharedContext?.put(IS_UAST_ANNOTATION_PARAMETER_AF, t, java.lang.Boolean.FALSE)
            return false
        }

        val accepts = uAnnotationQualifiedNamePattern(StandardPatterns.string().matches(annotationFqn))
            .accepts(containingUAnnotationEntry.first)
        return accepts && ((containingUAnnotationEntry.second ?: "value") == attribute)
    }
}
