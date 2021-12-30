package nl.jrdie.idea.springql.ide.highlighting

import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.openapi.components.service
import com.intellij.psi.PsiElement
import nl.jrdie.idea.springql.svc.QLIdeService
import org.jetbrains.uast.UMethod
import org.jetbrains.uast.toUElementOfType

// TODO: Spring merges the annotations using their utility class
//   https://github.com/spring-projects/spring-graphql/blob/main/spring-graphql/src/main/java/org/springframework/graphql/data/method/annotation/support/AnnotatedControllerConfigurer.java#L206
//   Add quick fix to simplify
class DuplicateSchemaMappingAnnotator : Annotator {

    override fun annotate(psiElement: PsiElement, holder: AnnotationHolder) {
        val svc = psiElement.project.service<QLIdeService>()
        if (!svc.isApplicableProject(psiElement.project)) {
            return
        }

        val uElement = psiElement.toUElementOfType<UMethod>()
        if (uElement !is UMethod) {
            return
        }

        val numSchemaMappingAnnotations = uElement
            .uAnnotations
            .filter(svc::isSchemaMappingAnnotation)

        if (numSchemaMappingAnnotations.size > 1) {
            numSchemaMappingAnnotations.forEach {
                holder
                    .newAnnotation(
                        HighlightSeverity.WEAK_WARNING,
                        "The mapping annotations will be reduced by Spring"
                    )
                    .range(it.sourcePsi!!)
                    .create()
            }
        }
    }
}
