package nl.jrdie.idea.springql.ide.highlighting

import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.openapi.components.service
import com.intellij.psi.PsiElement
import nl.jrdie.idea.springql.svc.QLIdeService
import org.jetbrains.uast.UMethod
import org.jetbrains.uast.toUElement

// Annotating a method with @SchemaMapping and @BatchMapping at once is prohibited.
//  See: TODO Insert URL here
class BatchMappingAndSchemaMappingErrorAnnotator : Annotator {

    override fun annotate(element: PsiElement, holder: AnnotationHolder) {
        val svc = element.project.service<QLIdeService>()
        if (!svc.isApplicableProject(element.project)) {
            return
        }

        val uElement = element.toUElement()
        if (uElement !is UMethod) {
            return
        }

        val isSchemaMapping = uElement.uAnnotations.filter(svc::isSchemaMappingAnnotation)

        if (isSchemaMapping.isEmpty()) {
            // Short circuit for performance.
            return
        }

        val isBatchMapping = uElement.uAnnotations.filter(svc::isBatchMappingAnnotation)

        if (isBatchMapping.isEmpty()) {
            return
        }

        // Annotate (highlight) offending annotations.
        (isBatchMapping union isSchemaMapping).forEach {
            println("Annotating ${it.qualifiedName}")
            holder
                .newAnnotation(
                    HighlightSeverity.ERROR,
                    "Methods can not be annotated with @SchemaMapping and @BatchMapping at the same time"
                )
                .range(it.sourcePsi!!)
                .create()
        }
    }
}
