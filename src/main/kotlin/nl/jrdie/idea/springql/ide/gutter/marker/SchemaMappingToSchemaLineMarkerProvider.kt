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
import com.intellij.openapi.components.service
import com.intellij.psi.PsiElement
import com.intellij.psi.util.nextLeaf
import nl.jrdie.idea.springql.icons.QLIcons
import nl.jrdie.idea.springql.svc.QLIdeService
import org.jetbrains.uast.UMethod
import org.jetbrains.uast.toUElementOfType

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
