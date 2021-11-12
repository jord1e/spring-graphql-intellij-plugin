package nl.jrdie.idea.springql.ide.gutter.marker

import com.intellij.codeInsight.daemon.RelatedItemLineMarkerInfo
import com.intellij.codeInsight.daemon.RelatedItemLineMarkerProvider
import com.intellij.codeInsight.navigation.NavigationGutterIconBuilder
import com.intellij.icons.AllIcons
import com.intellij.openapi.components.service
import com.intellij.psi.PsiElement
import com.intellij.psi.util.nextLeaf
import nl.jrdie.idea.springql.models.annotations.SchemaMappingType
import nl.jrdie.idea.springql.services.KaraIdeService
import org.jetbrains.uast.UAnnotation
import org.jetbrains.uast.toUElement

class SchemaMappingToSchemaLineMarkerProvider : RelatedItemLineMarkerProvider() {

    override fun collectNavigationMarkers(
        element: PsiElement,
        result: MutableCollection<in RelatedItemLineMarkerInfo<*>>
    ) {
        val uElement = element.toUElement()

        if (uElement !is UAnnotation) {
            return
        }

        if (!SchemaMappingType.isSchemaMappingAnnotation(uElement.qualifiedName!!)) {
//            println("NOT A SCHEMA MAPPING ANNOTATION: ${uElement.qualifiedName}")
            return
        }

        val graphQlService = element.project.service<KaraIdeService>()
        val index = graphQlService.getAnnotationIndex().findMappingsByAnnotation(uElement)

        val lineMarkerInfo = if (index.isEmpty()) {
            NavigationGutterIconBuilder.create(AllIcons.Gutter.WriteAccess)
                .setTooltipText("No schema declaration")
                .setTargets(emptyList())
        } else {
            NavigationGutterIconBuilder.create(AllIcons.Gutter.ReadAccess)
                .setTooltipText("Navigate to schema declaration")
                .setTargets(index.mapNotNull { it.graphQlSchemaPsi?.nextLeaf() })
        }

        result.add(lineMarkerInfo.createLineMarkerInfo(element.nextLeaf()!!))
    }

}