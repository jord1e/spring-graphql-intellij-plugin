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

package nl.jrdie.idea.springql.ide.completion.contributor

import com.intellij.psi.PsiReferenceContributor
import com.intellij.psi.PsiReferenceRegistrar
import com.intellij.psi.registerUastReferenceProvider
import nl.jrdie.idea.springql.ide.completion.QLSchemaArgumentNameRefProvider
import nl.jrdie.idea.springql.ide.completion.QLSchemaMappingFieldNameRefProvider
import nl.jrdie.idea.springql.ide.completion.QLSchemaMappingTypeNameRefProvider
import nl.jrdie.idea.springql.ide.completion.element.AliasForAttributeElementPattern
import nl.jrdie.idea.springql.utils.QLIdeUtil

class SchemaMappingContributor : PsiReferenceContributor() {

    override fun registerReferenceProviders(registrar: PsiReferenceRegistrar) {
        registrar.registerUastReferenceProvider(
            AliasForAttributeElementPattern(QLIdeUtil.SCHEMA_MAPPING_FQN, "typeName"),
            QLSchemaMappingTypeNameRefProvider()
        )
        registrar.registerUastReferenceProvider(
            AliasForAttributeElementPattern(QLIdeUtil.BATCH_MAPPING_FQN, "typeName"),
            QLSchemaMappingTypeNameRefProvider()
        )

        // @SchemaMapping
        registrar.registerUastReferenceProvider(
            AliasForAttributeElementPattern(QLIdeUtil.SCHEMA_MAPPING_FQN, "value"),
            QLSchemaMappingFieldNameRefProvider()
        )
        registrar.registerUastReferenceProvider(
            AliasForAttributeElementPattern(QLIdeUtil.SCHEMA_MAPPING_FQN, "field"),
            QLSchemaMappingFieldNameRefProvider()
        )

        // @BatchMapping
        registrar.registerUastReferenceProvider(
            AliasForAttributeElementPattern(QLIdeUtil.BATCH_MAPPING_FQN, "value"),
            QLSchemaMappingFieldNameRefProvider()
        )
        registrar.registerUastReferenceProvider(
            AliasForAttributeElementPattern(QLIdeUtil.BATCH_MAPPING_FQN, "field"),
            QLSchemaMappingFieldNameRefProvider()
        )

        // @QueryMapping
        registrar.registerUastReferenceProvider(
            AliasForAttributeElementPattern(QLIdeUtil.QUERY_MAPPING_FQN, "value"),
            QLSchemaMappingFieldNameRefProvider()
        )
        registrar.registerUastReferenceProvider(
            AliasForAttributeElementPattern(QLIdeUtil.QUERY_MAPPING_FQN, "name"),
            QLSchemaMappingFieldNameRefProvider()
        )

        // @MutationMapping
        registrar.registerUastReferenceProvider(
            AliasForAttributeElementPattern(QLIdeUtil.MUTATION_MAPPING_FQN, "value"),
            QLSchemaMappingFieldNameRefProvider()
        )
        registrar.registerUastReferenceProvider(
            AliasForAttributeElementPattern(QLIdeUtil.MUTATION_MAPPING_FQN, "name"),
            QLSchemaMappingFieldNameRefProvider()
        )

        // @SubscriptionMapping
        registrar.registerUastReferenceProvider(
            AliasForAttributeElementPattern(QLIdeUtil.SUBSCRIPTION_MAPPING_FQN, "value"),
            QLSchemaMappingFieldNameRefProvider()
        )
        registrar.registerUastReferenceProvider(
            AliasForAttributeElementPattern(QLIdeUtil.SUBSCRIPTION_MAPPING_FQN, "name"),
            QLSchemaMappingFieldNameRefProvider()
        )

        // @Argument
        registrar.registerUastReferenceProvider(
            AliasForAttributeElementPattern(QLIdeUtil.ARGUMENT_FQN, "value"),
            QLSchemaArgumentNameRefProvider()
        )
        registrar.registerUastReferenceProvider(
            AliasForAttributeElementPattern(QLIdeUtil.ARGUMENT_FQN, "name"),
            QLSchemaArgumentNameRefProvider()
        )
    }
}
