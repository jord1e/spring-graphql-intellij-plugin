package nl.jrdie.idea.springql.ide.highlighting

import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.openapi.components.service
import com.intellij.psi.PsiElement
import nl.jrdie.idea.springql.svc.QLIdeService
import org.jetbrains.uast.UMethod
import org.jetbrains.uast.toUElementOfType

class SchemaMappingDoesNotExistAnnotator : Annotator {

    override fun annotate(element: PsiElement, holder: AnnotationHolder) {
        val svc = element.project.service<QLIdeService>()
        if (!svc.isApplicableProject(element.project)) {
            return
        }

        val uMethod = element.toUElementOfType<UMethod>()
        if (uMethod !is UMethod) {
            return
        }

        val mappingSummary = svc.getSummaryForMethod(uMethod)

        if (mappingSummary == null) {
            return
        }

        if (mappingSummary.typeName.isBlank()) {
            holder.newAnnotation(HighlightSeverity.WARNING, "No type specified (or inherited)")
                .range(mappingSummary.annotationPsi)
                .create()
            return
        }

        val typeDefinition = svc.schemaRegistry.getObjectTypeDefinition(mappingSummary.typeName)
        if (typeDefinition == null) {
            holder.newAnnotation(HighlightSeverity.WARNING, "Type ${mappingSummary.typeName} might not exist")
                .range(mappingSummary.annotationPsi)
                .create()
            return
        }

        val doesFieldExistOnType = typeDefinition
            .fieldDefinitions
            .any { it.name == mappingSummary.fieldName }
        if (!doesFieldExistOnType) {
            holder.newAnnotation(
                HighlightSeverity.WARNING,
                "Field ${mappingSummary.typeName}.${mappingSummary.fieldName} might not exist"
            ).range(mappingSummary.annotationPsi)
                .create()
        }
    }
}
