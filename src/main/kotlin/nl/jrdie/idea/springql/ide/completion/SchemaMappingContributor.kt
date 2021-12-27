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

import com.intellij.patterns.uast.ULiteralExpressionPattern
import com.intellij.psi.PsiReferenceContributor
import com.intellij.psi.PsiReferenceRegistrar
import com.intellij.psi.registerUastReferenceProvider
import nl.jrdie.idea.springql.models.annotations.SchemaMappingType

class SchemaMappingContributor : PsiReferenceContributor() {

    override fun registerReferenceProviders(registrar: PsiReferenceRegistrar) {
        registrar.registerUastReferenceProvider(
            ULiteralExpressionPattern().annotationParam(SchemaMappingType.SCHEMA_MAPPING.qualifiedAnnotationName, "typeName"),
            QLSchemaMappingTypeNameRefProvider()
        )

        // @SchemaMapping
        registrar.registerUastReferenceProvider(
            ULiteralExpressionPattern().annotationParam(SchemaMappingType.SCHEMA_MAPPING.qualifiedAnnotationName, "value"),
            QLSchemaMappingFieldNameRefProvider()
        )
        registrar.registerUastReferenceProvider(
            ULiteralExpressionPattern().annotationParam(SchemaMappingType.SCHEMA_MAPPING.qualifiedAnnotationName, "field"),
            QLSchemaMappingFieldNameRefProvider()
        )

        // @QueryMapping
        registrar.registerUastReferenceProvider(
            ULiteralExpressionPattern().annotationParam(SchemaMappingType.QUERY_MAPPING.qualifiedAnnotationName, "value"),
            QLSchemaMappingFieldNameRefProvider()
        )
        registrar.registerUastReferenceProvider(
            ULiteralExpressionPattern().annotationParam(SchemaMappingType.QUERY_MAPPING.qualifiedAnnotationName, "name"),
            QLSchemaMappingFieldNameRefProvider()
        )

        // @MutationMapping
        registrar.registerUastReferenceProvider(
            ULiteralExpressionPattern().annotationParam(SchemaMappingType.MUTATION_MAPPING.qualifiedAnnotationName, "value"),
            QLSchemaMappingFieldNameRefProvider()
        )
        registrar.registerUastReferenceProvider(
            ULiteralExpressionPattern().annotationParam(SchemaMappingType.MUTATION_MAPPING.qualifiedAnnotationName, "name"),
            QLSchemaMappingFieldNameRefProvider()
        )

        // @SubscriptionMapping
        registrar.registerUastReferenceProvider(
            ULiteralExpressionPattern().annotationParam(SchemaMappingType.SUBSCRIPTION_MAPPING.qualifiedAnnotationName, "value"),
            QLSchemaMappingFieldNameRefProvider()
        )
        registrar.registerUastReferenceProvider(
            ULiteralExpressionPattern().annotationParam(SchemaMappingType.SUBSCRIPTION_MAPPING.qualifiedAnnotationName, "name"),
            QLSchemaMappingFieldNameRefProvider()
        )
    }
}
