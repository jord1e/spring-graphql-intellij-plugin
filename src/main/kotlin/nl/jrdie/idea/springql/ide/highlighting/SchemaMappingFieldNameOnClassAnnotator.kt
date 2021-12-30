package nl.jrdie.idea.springql.ide.highlighting

import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.openapi.components.service
import com.intellij.psi.PsiElement
import nl.jrdie.idea.springql.svc.QLIdeService
import org.jetbrains.uast.UClass
import org.jetbrains.uast.toUElementOfType

class SchemaMappingFieldNameOnClassAnnotator : Annotator {

    override fun annotate(element: PsiElement, holder: AnnotationHolder) {
        val svc = element.project.service<QLIdeService>()
        if (!svc.isApplicableProject(element.project)) {
            return
        }

        val uClass = element.toUElementOfType<UClass>()
        if (uClass !is UClass) {
            return
        }

        for (uAnnotation in uClass.uAnnotations) {
            val mappings = svc.index.schemaMappingByAnnotation(uAnnotation)
            if (mappings.isEmpty()) {
                continue
            }

            if (mappings.any { !it.field.isNullOrEmpty() }) {
                holder.newAnnotation(
                    HighlightSeverity.WEAK_WARNING,
                    "Specifying fields on class level @SchemaMapping annotations does nothing"
                ).range(uAnnotation.sourcePsi!!)
                    .create()
            }
        }
    }
}
