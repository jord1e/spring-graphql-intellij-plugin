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

import com.intellij.psi.PsiReference
import com.intellij.psi.UastReferenceProvider
import com.intellij.util.ProcessingContext
import nl.jrdie.idea.springql.references.QLTypeNamePolyReference
import org.jetbrains.uast.UElement
import org.jetbrains.uast.UNamedExpression
import org.jetbrains.uast.expressions.UInjectionHost
import org.jetbrains.uast.getParentOfType

class QLSchemaMappingTypeNameRefProvider : UastReferenceProvider(UInjectionHost::class.java) {

    override fun getReferencesByElement(element: UElement, context: ProcessingContext): Array<PsiReference> {
        if (element !is UInjectionHost) {
            throw IllegalStateException("Should not happen")
        }

        if (element.sourcePsi?.project == null) {
            return PsiReference.EMPTY_ARRAY
        }

        val attribute = element.getParentOfType<UNamedExpression>()?.name
        if (attribute != "typeName") {
            return PsiReference.EMPTY_ARRAY
        }

        return arrayOf(QLTypeNamePolyReference(element.sourcePsi!!))
    }
}
