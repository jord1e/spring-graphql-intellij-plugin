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