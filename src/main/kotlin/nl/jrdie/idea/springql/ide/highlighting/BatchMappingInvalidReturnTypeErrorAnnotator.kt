package nl.jrdie.idea.springql.ide.highlighting

import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.openapi.components.service
import com.intellij.psi.PsiElement
import nl.jrdie.idea.springql.svc.QLIdeService
import org.jetbrains.uast.UMethod
import org.jetbrains.uast.toUElement

class BatchMappingInvalidReturnTypeErrorAnnotator : Annotator {

    override fun annotate(element: PsiElement, holder: AnnotationHolder) {
        val svc = element.project.service<QLIdeService>()
        if (!svc.isApplicableProject(element.project)) {
            return
        }

        val uElement = element.toUElement()
        if (uElement !is UMethod) {
            return
        }

        val isBatchMapping = uElement.uAnnotations
            .filter(svc::isBatchMappingAnnotation)

        if (isBatchMapping.isEmpty()) {
            return
        }

        // TODO Handle nested generic Mono<Map<K, V>> instead of Mono<V>.
        if (svc.isValidBatchMappingReturnType(uElement)) {
            return
        }

        for (it in isBatchMapping) {
            holder
                .newAnnotation(
                    HighlightSeverity.ERROR,
                    "Methods annotated with @BatchMapping should return an instance of Flux<V>, List<V>, Mono<Map<K, V>>, or Map<K, V>"
                )
                .range(it.sourcePsi!!)
                .create()
        }
    }
}
