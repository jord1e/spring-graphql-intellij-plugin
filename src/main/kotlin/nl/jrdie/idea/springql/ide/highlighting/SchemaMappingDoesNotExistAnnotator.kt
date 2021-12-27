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
