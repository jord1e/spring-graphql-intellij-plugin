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

import com.intellij.openapi.components.service
import com.intellij.psi.PsiReference
import com.intellij.psi.UastReferenceProvider
import com.intellij.util.ProcessingContext
import nl.jrdie.idea.springql.ide.references.GraphQlFieldReference
import nl.jrdie.idea.springql.svc.QLIdeService
import org.jetbrains.uast.UElement
import org.jetbrains.uast.ULiteralExpression
import org.jetbrains.uast.UNamedExpression
import org.jetbrains.uast.getParentOfType
import org.jetbrains.uast.kotlin.KotlinStringULiteralExpression

@Suppress("FoldInitializerAndIfToElvis")
class QLSchemaMappingFieldNameRefProvider : UastReferenceProvider(ULiteralExpression::class.java) {

    override fun getReferencesByElement(element: UElement, context: ProcessingContext): Array<PsiReference> {
        if (element !is ULiteralExpression) {
            throw IllegalStateException("Should not happen")
        }

        // TODO Fix this bug with Kotlin.
        if (element is KotlinStringULiteralExpression) {
            return PsiReference.EMPTY_ARRAY
        }

        val sourcePsi = element.sourcePsi
        if (sourcePsi == null /* || sourcePsi.project == null */) {
            return PsiReference.EMPTY_ARRAY
        }

        val svc = sourcePsi.project.service<QLIdeService>()
        if (!svc.isApplicableProject(sourcePsi.project)) {
            return PsiReference.EMPTY_ARRAY
        }

        val attribute = element.getParentOfType<UNamedExpression>()?.name
        // TODO Mapping specific, or use Spring
        if (attribute != "value" && attribute != "name" && attribute != "field") {
            return PsiReference.EMPTY_ARRAY
        }

        return arrayOf(GraphQlFieldReference(sourcePsi))
    }
}
