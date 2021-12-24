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

package nl.jrdie.idea.springql.ide

import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.lang.jsgraphql.icons.GraphQLIcons
import com.intellij.openapi.components.service
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementResolveResult
import com.intellij.psi.PsiPolyVariantReferenceBase
import com.intellij.psi.ResolveResult
import nl.jrdie.idea.springql.svc.QLIdeService

class GraphQlTypeNameReference(element: PsiElement) : PsiPolyVariantReferenceBase<PsiElement>(element) {

    override fun multiResolve(incompleteCode: Boolean): Array<ResolveResult> {
        val svc = myElement.project.service<QLIdeService>()

        return svc
            .schemaRegistry
            .objectDefinitions
            .map { PsiElementResolveResult(it.element!!) }
            .toTypedArray()
    }

    override fun getVariants(): Array<Any> {
        val svc = myElement.project.service<QLIdeService>()

        return svc
            .schemaRegistry
            .objectDefinitions
            .map { typeDefinition ->
                LookupElementBuilder
                    .create(typeDefinition.name)
                    .withPsiElement(typeDefinition.element!!)
                    .let {
                        val icon = when (typeDefinition.name) {
                            "Query" -> GraphQLIcons.Schema.Query
                            "Mutation" -> GraphQLIcons.Schema.Mutation
                            "Subscription" -> GraphQLIcons.Schema.Subscription
                            else -> GraphQLIcons.Schema.Type
                        }
                        it.withIcon(icon)
                    }
            }
            .toTypedArray()
    }

}