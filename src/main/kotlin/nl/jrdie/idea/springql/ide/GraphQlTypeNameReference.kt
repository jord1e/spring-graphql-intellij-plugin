package nl.jrdie.idea.springql.ide

import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.lang.jsgraphql.icons.JSGraphQLIcons
import com.intellij.lang.jsgraphql.types.language.ObjectTypeDefinition
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementResolveResult
import com.intellij.psi.PsiPolyVariantReferenceBase
import com.intellij.psi.ResolveResult
import nl.jrdie.idea.springql.services.getKaraService

class GraphQlTypeNameReference(element: PsiElement) : PsiPolyVariantReferenceBase<PsiElement>(element) {

    override fun multiResolve(incompleteCode: Boolean): Array<ResolveResult> {
        val graphQlIdeService = myElement.project.getKaraService()

        return graphQlIdeService
            .getTypeDefinitionRegistry(myElement.project)
            .getTypes(ObjectTypeDefinition::class.java)
            .map { PsiElementResolveResult(it.element!!) }
            .toTypedArray()
    }

    override fun getVariants(): Array<Any> {
        val graphQlIdeService = myElement.project.getKaraService()

        return graphQlIdeService
            .getTypeDefinitionRegistry(myElement.project)
            .getTypes(ObjectTypeDefinition::class.java)
            .map { typeDefinition ->
                LookupElementBuilder
                    .create(typeDefinition.name)
                    .withPsiElement(typeDefinition.element!!)
                    .let {
                        val icon = when (typeDefinition.name) {
                            "Query" -> JSGraphQLIcons.Schema.Query
                            "Mutation" -> JSGraphQLIcons.Schema.Mutation
                            "Subscription" -> JSGraphQLIcons.Schema.Subscription
                            else -> JSGraphQLIcons.Schema.Type
                        }
                        it.withIcon(icon)
                    }
            }
            .toTypedArray()
    }

}