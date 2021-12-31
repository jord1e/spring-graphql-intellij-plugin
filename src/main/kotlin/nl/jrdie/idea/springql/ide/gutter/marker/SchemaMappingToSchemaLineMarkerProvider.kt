package nl.jrdie.idea.springql.ide.gutter.marker

import com.intellij.codeInsight.daemon.RelatedItemLineMarkerInfo
import com.intellij.codeInsight.daemon.RelatedItemLineMarkerProvider
import com.intellij.codeInsight.navigation.NavigationGutterIconBuilder
import com.intellij.openapi.components.service
import com.intellij.psi.PsiElement
import com.intellij.psi.util.nextLeaf
import nl.jrdie.idea.springql.icons.QLIcons
import nl.jrdie.idea.springql.svc.QLIdeService
import org.jetbrains.uast.UMethod
import org.jetbrains.uast.toUElementOfType

@Suppress("FoldInitializerAndIfToElvis")
class SchemaMappingToSchemaLineMarkerProvider : RelatedItemLineMarkerProvider() {

    override fun collectNavigationMarkers(
        element: PsiElement,
        result: MutableCollection<in RelatedItemLineMarkerInfo<*>>
    ) {
        val svc = element.project.service<QLIdeService>()
        if (!svc.isApplicableProject(element.project)) {
            return
        }

        val uMethod = element.toUElementOfType<UMethod>()

        if (uMethod !is UMethod) {
            return
        }

        val mappingSummary = svc.getSummaryForMethod(uMethod)

//        println("Summary for ${uMethod.name} -> $mappingSummary")

        if (mappingSummary == null) {
            return
        }

        val lineMarkerInfo = if (mappingSummary.schemaPsi == null) {
            NavigationGutterIconBuilder.create(QLIcons.SpringGraphGutterGreyQL)
                .setTooltipText("No schema declaration")
                .setTargets(emptyList())
        } else {
            NavigationGutterIconBuilder.create(QLIcons.SpringGraphGutterGreenQL)
                .setTooltipText("Navigate to schema declaration")
                .setTargets(mappingSummary.schemaPsi)
        }

        result.add(lineMarkerInfo.createLineMarkerInfo(mappingSummary.annotationPsi.nextLeaf()!!))
    }
}
