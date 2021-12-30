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
