package nl.jrdie.idea.springql.ide.highlighting

import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.psi.PsiElement
import nl.jrdie.idea.springql.utils.KaraIdeUtil
import org.jetbrains.uast.UMethod
import org.jetbrains.uast.toUElement

class BatchMappingAndSchemaMappingErrorAnnotator : Annotator {

    override fun annotate(psiElement: PsiElement, holder: AnnotationHolder) {
        val uElement = psiElement.toUElement();
        if (uElement !is UMethod) {
            return
        }

        val anyIsSchemaMapping = uElement
            .uAnnotations
            .filter(KaraIdeUtil::isSchemaMappingAnnotation)

        if (anyIsSchemaMapping.isEmpty()) {
            return // Short circuit for performance
        }

        val anyIsBatchMapping = uElement
            .uAnnotations
            .filter(KaraIdeUtil::isBatchMappingAnnotation)

        // If only one of the mapping types is present, short circuit
        if (anyIsBatchMapping.isEmpty()) {
            return
        }
//anyIsBatchMapping.forEach { println("A::: " + it) }
        // Annotate offending annotations
        (anyIsBatchMapping union anyIsSchemaMapping).forEach {
            println("Annotating ${it.qualifiedName}")
            holder
                .newAnnotation(
                    HighlightSeverity.ERROR,
                    "Methods can not have both @SchemaMapping and @BatchMapping annotations at the same time"
                )
                .range(it.sourcePsi!!)
                .create()
        }
    }
}