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

package nl.jrdie.idea.springql.ide.references

import com.intellij.codeInsight.completion.PrioritizedLookupElement
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.lang.jsgraphql.types.language.ObjectTypeDefinition
import com.intellij.openapi.components.service
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementResolveResult
import com.intellij.psi.PsiPolyVariantReferenceBase
import com.intellij.psi.ResolveResult
import nl.jrdie.idea.springql.icons.QLIcons
import nl.jrdie.idea.springql.svc.QLIdeService
import javax.swing.Icon

class GraphQlTypeNameReference(element: PsiElement) : PsiPolyVariantReferenceBase<PsiElement>(element) {

    companion object {
        fun decidePriority(typeDefinition: ObjectTypeDefinition): Double {
            return when {
                typeDefinition.name.startsWith("__") -> 0.5
                typeDefinition.name.startsWith('_') -> 0.0
                else -> 1.0
            }
        }
    }

    override fun multiResolve(incompleteCode: Boolean): Array<ResolveResult> {
        val svc = myElement.project.service<QLIdeService>()

        return svc
            .schemaRegistry
            .objectDefinitions
            .map { PsiElementResolveResult(it.element!!) }
            .toTypedArray()
    }

    override fun getVariants(): Array<LookupElement> {
        val svc = myElement.project.service<QLIdeService>()

        return svc
            .schemaRegistry
            .objectDefinitions
            .map { typeDefinition ->
                val icon: Icon = when {
                    svc.isApolloFederationNode(typeDefinition) -> QLIcons.Apollo
                    svc.isIntrospectionNode(typeDefinition) -> QLIcons.IntrospectionFieldType
                    else -> when (typeDefinition.name) {
                        "Query" -> QLIcons.Query
                        "Mutation" -> QLIcons.Mutation
                        "Subscription" -> QLIcons.Subscription
                        else -> QLIcons.Type
                    }
                }

                PrioritizedLookupElement.withPriority(
                    LookupElementBuilder
                        .create(typeDefinition.name)
                        .withPsiElement(typeDefinition.element!!)
                        .withIcon(icon),
                    decidePriority(typeDefinition)
                )
            }
            .toTypedArray()
    }
}
