package nl.jrdie.idea.springql.ide.highlighting

import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.psi.PsiElement
import nl.jrdie.idea.springql.utils.KaraIdeUtil
import org.jetbrains.uast.UMethod
import org.jetbrains.uast.toUElement

// TODO: Spring merges the annotations using their utility class
//   https://github.com/spring-projects/spring-graphql/blob/main/spring-graphql/src/main/java/org/springframework/graphql/data/method/annotation/support/AnnotatedControllerConfigurer.java#L206
//   Add quick fix to simplify
class DuplicateSchemaMappingAnnotator : Annotator {

    override fun annotate(psiElement: PsiElement, holder: AnnotationHolder) {
        val uElement = psiElement.toUElement();
        if (uElement !is UMethod) {
            return
        }

        val numSchemaMappingAnnotations = uElement
            .uAnnotations
            .filter(KaraIdeUtil::isSchemaMappingAnnotation)

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