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
import com.intellij.icons.AllIcons
import com.intellij.openapi.components.service
import com.intellij.psi.PsiElement
import com.intellij.psi.util.nextLeaf
import nl.jrdie.idea.springql.svc.QLIdeService
import org.jetbrains.uast.UAnnotation
import org.jetbrains.uast.toUElement

class SchemaMappingToSchemaLineMarkerProvider : RelatedItemLineMarkerProvider() {

    override fun collectNavigationMarkers(
        element: PsiElement,
        result: MutableCollection<in RelatedItemLineMarkerInfo<*>>
    ) {
        val svc = element.project.service<QLIdeService>()
        if (!svc.isApplicableProject(element.project)) {
            return
        }

        val uElement = element.toUElement()

        if (uElement !is UAnnotation) {
            return
        }

        if (!svc.isSchemaMappingAnnotation(uElement)) {
//            println("NOT A SCHEMA MAPPING ANNOTATION: ${uElement.qualifiedName}")
            return
        }

        val index = svc.index.schemaMappingByAnnotation(uElement)
        println("Index for $uElement: (${index.size}) $index")

        val lineMarkerInfo = if (index.isEmpty() || index.none { it.schemaPsi.isNotEmpty() }) {
            NavigationGutterIconBuilder.create(AllIcons.Gutter.WriteAccess)
                .setTooltipText("No schema declaration")
                .setTargets(emptyList())
        } else {
            NavigationGutterIconBuilder.create(AllIcons.Gutter.ReadAccess)
                .setTooltipText("Navigate to schema declaration")
                .setTargets(index.flatMap {it.schemaPsi}.mapNotNull { it?.nextLeaf() })
        }

        result.add(lineMarkerInfo.createLineMarkerInfo(element.nextLeaf()!!))
    }

}