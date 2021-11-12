/*
 * Copyright (C) 2021 Jordie
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package nl.jrdie.idea.springql.ide.gutter.marker

import com.intellij.codeInsight.daemon.RelatedItemLineMarkerInfo
import com.intellij.codeInsight.daemon.RelatedItemLineMarkerProvider
import com.intellij.codeInsight.navigation.NavigationGutterIconBuilder
import com.intellij.lang.jsgraphql.icons.JSGraphQLIcons
import com.intellij.lang.jsgraphql.psi.GraphQLFieldDefinition
import com.intellij.lang.jsgraphql.psi.GraphQLPsiUtil
import com.intellij.openapi.components.service
import com.intellij.psi.PsiElement
import com.intellij.psi.util.nextLeaf
import nl.jrdie.idea.springql.services.KaraIdeService

class SchemaToSchemaMappingLineMarkerProvider : RelatedItemLineMarkerProvider() {

    override fun collectNavigationMarkers(
        element: PsiElement,
        result: MutableCollection<in RelatedItemLineMarkerInfo<*>>
    ) {
        if (element !is GraphQLFieldDefinition) {
            return
        }

        val graphQlService = element.project.service<KaraIdeService>()
        val indexEntry = graphQlService.getAnnotationIndex().findMappingsByFieldDefinition(element)
        if (indexEntry.isNotEmpty()) {
            val lineMarkerInfo = NavigationGutterIconBuilder
//                .create(AllIcons.Ide.Link)
                .create(JSGraphQLIcons.UI.GraphQLToolwindow)
                .setPopupTitle("Controller mappings for ${GraphQLPsiUtil.getTypeName(element, null)}.${element.nameIdentifier.text}")
                .setTooltipText("Navigate to controller mapping")
                .setTargets(indexEntry.mapNotNull { it.annotation.sourcePsi?.nextLeaf() })
                .setEmptyPopupText("No controller mappings")
                .createLineMarkerInfo(element.nextLeaf()!!)
            result.add(lineMarkerInfo)
        }

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