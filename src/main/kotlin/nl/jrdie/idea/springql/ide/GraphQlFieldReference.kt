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
import com.intellij.lang.jsgraphql.icons.JSGraphQLIcons
import com.intellij.lang.jsgraphql.types.language.ObjectTypeDefinition
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementResolveResult
import com.intellij.psi.PsiPolyVariantReferenceBase
import com.intellij.psi.ResolveResult
import nl.jrdie.idea.springql.services.getKaraService

class GraphQlFieldReference(element: PsiElement) : PsiPolyVariantReferenceBase<PsiElement>(element) {

    override fun multiResolve(incompleteCode: Boolean): Array<ResolveResult> {
        val graphQlIdeService = myElement.project.getKaraService()

        return graphQlIdeService
            .getTypeDefinitionRegistry(myElement.project)
            .getTypes(ObjectTypeDefinition::class.java)
            .flatMap { it.fieldDefinitions }
            .map { PsiElementResolveResult(it.element!!) }
            .toTypedArray()
    }

    override fun getVariants(): Array<Any> {
        val graphQlIdeService = myElement.project.getKaraService()

        return graphQlIdeService
            .getTypeDefinitionRegistry(myElement.project)
            .getTypes(ObjectTypeDefinition::class.java)
            .flatMap { it.fieldDefinitions }
            .map { typeDefinition ->
                LookupElementBuilder
                    .create(typeDefinition.name)
                    .withPsiElement(typeDefinition.element!!)
                    .withIcon(JSGraphQLIcons.Schema.Field)
                    .withTypeText(typeDefinition.type.element!!.text)
            }
            .toTypedArray()
    }

}