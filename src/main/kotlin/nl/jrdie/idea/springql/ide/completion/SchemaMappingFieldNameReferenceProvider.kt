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

package nl.jrdie.idea.springql.ide.completion

import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReference
import com.intellij.psi.PsiReferenceProvider
import com.intellij.util.ProcessingContext
import nl.jrdie.idea.springql.ide.GraphQlFieldReference
import org.jetbrains.uast.ULiteralExpression
import org.jetbrains.uast.UNamedExpression
import org.jetbrains.uast.getParentOfType
import org.jetbrains.uast.kotlin.KotlinStringULiteralExpression
import org.jetbrains.uast.toUElement

class SchemaMappingFieldNameReferenceProvider : PsiReferenceProvider() {

    override fun getReferencesByElement(element: PsiElement, context: ProcessingContext): Array<PsiReference> {
        val uElement = element.toUElement()
        if (uElement !is ULiteralExpression) {
            throw IllegalStateException("Should not happen")
//                return PsiReference.EMPTY_ARRAY
        }

        if (uElement is KotlinStringULiteralExpression) {
            return PsiReference.EMPTY_ARRAY
        }

        val attribute = uElement.getParentOfType<UNamedExpression>()?.name
        // TODO Mapping specific, or use Spring
        if (attribute != "value" && attribute != "name" && attribute != "field") {
            return PsiReference.EMPTY_ARRAY
        }

        val project = uElement.sourcePsi?.project
        if (project == null) {
            return PsiReference.EMPTY_ARRAY
        }

        return arrayOf(GraphQlFieldReference(uElement.sourcePsi!!))
    }

}