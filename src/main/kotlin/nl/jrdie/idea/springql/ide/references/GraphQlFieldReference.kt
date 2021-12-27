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
import com.intellij.lang.jsgraphql.types.language.FieldDefinition
import com.intellij.lang.jsgraphql.types.language.ObjectTypeDefinition
import com.intellij.openapi.components.service
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementResolveResult
import com.intellij.psi.PsiPolyVariantReferenceBase
import com.intellij.psi.ResolveResult
import nl.jrdie.idea.springql.icons.QLIcons
import nl.jrdie.idea.springql.svc.QLIdeService
import org.jetbrains.uast.UMethod
import org.jetbrains.uast.getParentOfType
import org.jetbrains.uast.toUElement
import javax.swing.Icon

@Suppress("FoldInitializerAndIfToElvis")
class GraphQlFieldReference(element: PsiElement) : PsiPolyVariantReferenceBase<PsiElement>(element) {

    override fun multiResolve(incompleteCode: Boolean): Array<ResolveResult> {
        val graphQlIdeService = myElement.project.service<QLIdeService>()

        return graphQlIdeService
            .schemaRegistry
            .fieldDefinitions
            .mapNotNull(FieldDefinition::getElement)
            .map(::PsiElementResolveResult)
            .toTypedArray()
    }

    override fun getVariants(): Array<LookupElement> {
        val svc = myElement.project.service<QLIdeService>()

        fun createLookupElement(fieldDefinition: FieldDefinition): LookupElement? {
            println("Creating LookupElement for $fieldDefinition -> ${fieldDefinition.sourceNodes}")
            val targetElement = fieldDefinition.element
            if (targetElement == null) {
                return null
            }

            val nearestAnnotation = svc.findNearestSchemaMappingAnnotations(myElement.toUElement())
                .firstOrNull()

            val parentTypeByAnnotation: ObjectTypeDefinition? = when (nearestAnnotation) {
                null -> null
                else -> {
                    val parentTypeName = svc.findApplicableParentTypeName(nearestAnnotation)
                    if (parentTypeName == null) {
                        return null
                    }

                    svc.schemaRegistry.getObjectTypeDefinition(parentTypeName)
                }
            }

            val isChildOfCurrentAnnotationParentType = parentTypeByAnnotation
                ?.fieldDefinitions?.contains(fieldDefinition) ?: false

            val typeText: String? = fieldDefinition.type?.element?.text

            fun getIcon(): Icon = when {
                svc.isApolloFederationNode(fieldDefinition) -> QLIcons.Apollo
                svc.isIntrospectionNode(fieldDefinition) -> QLIcons.IntrospectionFieldType
                else -> QLIcons.Field
            }

            val correctName: String = if (isChildOfCurrentAnnotationParentType) fieldDefinition.name else
                (svc.schemaRegistry.getParentType(fieldDefinition)?.name?.plus(".") ?: "") + fieldDefinition.name

            val lookupElement = LookupElementBuilder
                .create(fieldDefinition.name)
                .withPresentableText(correctName)
                .withPsiElement(fieldDefinition.element!!)
                .withIcon(getIcon())
                .withBoldness(isChildOfCurrentAnnotationParentType)
                .withStrikeoutness(fieldDefinition.hasDirective("deprecated"))
                .withTypeText(typeText)

            return PrioritizedLookupElement
                .withPriority(lookupElement, determinePriority(fieldDefinition, parentTypeByAnnotation, svc))
        }

        return svc
            .schemaRegistry
            .fieldDefinitions
            .mapNotNull(::createLookupElement)
            .toTypedArray()
    }

    private fun determinePriority(
        fieldDefinition: FieldDefinition,
        parentType: ObjectTypeDefinition?,
        svc: QLIdeService
    ): Double {
        if (svc.isIntrospectionNode(fieldDefinition)) {
            return 0.0
        }

        if (fieldDefinition.hasDirective("deprecated")) {
            return 7.0
        }

        val nearestMethod = myElement.toUElement()?.getParentOfType<UMethod>()
        if (nearestMethod != null) {
            if (fieldDefinition.name.equals(nearestMethod.name, true)) {
                return 10.0
            }
        }

        if (parentType != null && parentType.fieldDefinitions.contains(fieldDefinition)) {
            return 8.0
        }

        return 5.0
    }

}
