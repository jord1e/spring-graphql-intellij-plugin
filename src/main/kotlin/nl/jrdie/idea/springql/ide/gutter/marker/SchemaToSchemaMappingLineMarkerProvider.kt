package nl.jrdie.idea.springql.ide.gutter.marker

import com.intellij.codeInsight.daemon.RelatedItemLineMarkerInfo
import com.intellij.codeInsight.daemon.RelatedItemLineMarkerProvider
import com.intellij.codeInsight.navigation.NavigationGutterIconBuilder
import com.intellij.lang.jsgraphql.psi.GraphQLFieldDefinition
import com.intellij.openapi.components.service
import com.intellij.psi.PsiElement
import com.intellij.psi.util.nextLeaf
import nl.jrdie.idea.springql.icons.QLIcons
import nl.jrdie.idea.springql.svc.QLIdeService

class SchemaToSchemaMappingLineMarkerProvider : RelatedItemLineMarkerProvider() {

    override fun collectNavigationMarkers(
        element: PsiElement,
        result: MutableCollection<in RelatedItemLineMarkerInfo<*>>
    ) {
        if (element !is GraphQLFieldDefinition) {
            // Fastest check, so put it up top
            return
        }

        val svc = element.project.service<QLIdeService>()
        if (!svc.isApplicableProject(element.project)) {
            return
        }

        val index = svc.index.schemaMappingBySchemaPsi(element)
        if (index.isEmpty()) {
            return
        }

//        val graphQlService = element.project.service<QLIdeServiceImpl>()
//        val indexEntry = graphQlService.getAnnotationIndex().findMappingsByFieldDefinition(element)
//        if (indexEntry.isNotEmpty()) {
        val lineMarkerInfo = NavigationGutterIconBuilder
//                .create(AllIcons.Ide.Link)
            .create(QLIcons.SpringGraphGutterGreyQL)
//            .setPopupTitle(
// //                    "Controller mappings for ${
// //                        GraphQLPsiUtil.getTypeName(
// //                            element,
// //                            null
// //                        )
// //                    }.${element.nameIdentifier.text}"
//            )
            .setTooltipText("Navigate to controller mapping")
            .setTargets(index.mapNotNull { it.annotationPsi.nextLeaf() })
            .setEmptyPopupText("No controller mappings")
            .createLineMarkerInfo(element.nextLeaf()!!)

        result.add(lineMarkerInfo)
//        }

//        if (indexEntry == null) {
//            return
//        }
//
//        val lineMarkerInfo = NavigationGutterIconBuilder
//            .create(AllIcons.Ide.Link)
//            .setTooltipText("Navigate to controller mapping")
//            .setTargets(indexEntry.annotation.sourcePsi!!)
//            .createLineMarkerInfo(element)
//        result.add(lineMarkerInfo)
    }
}
