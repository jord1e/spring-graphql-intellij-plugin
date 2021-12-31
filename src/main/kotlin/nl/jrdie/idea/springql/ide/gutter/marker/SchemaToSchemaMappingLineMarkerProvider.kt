package nl.jrdie.idea.springql.ide.gutter.marker

import com.intellij.codeInsight.daemon.RelatedItemLineMarkerInfo
import com.intellij.codeInsight.daemon.RelatedItemLineMarkerProvider
import com.intellij.codeInsight.navigation.NavigationGutterIconBuilder
import com.intellij.lang.jsgraphql.psi.GraphQLFieldDefinition
import com.intellij.openapi.components.service
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiManager
import com.intellij.psi.util.nextLeaf
import nl.jrdie.idea.springql.icons.QLIcons
import nl.jrdie.idea.springql.svc.QLIdeService

class SchemaToSchemaMappingLineMarkerProvider : RelatedItemLineMarkerProvider() {

    override fun collectNavigationMarkers(
        element: PsiElement,
        result: MutableCollection<in RelatedItemLineMarkerInfo<*>>
    ) {
        if (element !is GraphQLFieldDefinition) {
            return
        }

        val svc = element.project.service<QLIdeService>()
        if (!svc.isApplicableProject(element.project)) {
            return
        }

        @Suppress("DEPRECATION")
        val index = svc.thoroughSummaryView
            .filter { PsiManager.getInstance(element.project).areElementsEquivalent(it.schemaPsi, element) }
        if (index.isEmpty()) {
            return
        }

        val lineMarkerInfo = NavigationGutterIconBuilder
            .create(QLIcons.SpringGraphGutterGreenQL)
            .setTooltipText("Navigate to controller mapping")
            .setTargets(index.mapNotNull { it.annotationPsi.nextLeaf() })
            .setEmptyPopupText("No controller mappings")
            .createLineMarkerInfo(element.nextLeaf()!!)

        result.add(lineMarkerInfo)
    }
}
