package nl.jrdie.idea.springql.ide.completion

import com.intellij.patterns.PsiJavaPatterns
import com.intellij.patterns.StandardPatterns
import com.intellij.psi.PsiReferenceContributor
import com.intellij.psi.PsiReferenceRegistrar
import nl.jrdie.idea.springql.models.annotations.SchemaMappingType
import org.jetbrains.kotlin.idea.completion.or

class SchemaMappingContributor : PsiReferenceContributor() {

    override fun registerReferenceProviders(registrar: PsiReferenceRegistrar) {
        registrar.registerReferenceProvider(
            PsiJavaPatterns.psiElement()
                .insideAnnotationParam(
                    StandardPatterns.string().equalTo(SchemaMappingType.SCHEMA_MAPPING.qualifiedAnnotationName),
                    "typeName"
                ),
            SchemaMappingAnnotationGraphQlReferenceProvider()
        )
        registrar.registerReferenceProvider(
            PsiJavaPatterns.psiElement().insideAnnotationParam(
                StandardPatterns.string().equalTo(SchemaMappingType.SCHEMA_MAPPING.qualifiedAnnotationName),
                "value"
            ).or(
                PsiJavaPatterns.psiElement().insideAnnotationParam(
                    StandardPatterns.string().equalTo(SchemaMappingType.SCHEMA_MAPPING.qualifiedAnnotationName),
                    "field"
                )
            ).or(
                PsiJavaPatterns.psiElement().insideAnnotationParam(
                    StandardPatterns.string().equalTo(SchemaMappingType.QUERY_MAPPING.qualifiedAnnotationName),
                    "value"
                )
            ).or(
                PsiJavaPatterns.psiElement().insideAnnotationParam(
                    StandardPatterns.string().equalTo(SchemaMappingType.QUERY_MAPPING.qualifiedAnnotationName),
                    "name"
                )
            ).or(
                PsiJavaPatterns.psiElement().insideAnnotationParam(
                    StandardPatterns.string().equalTo(SchemaMappingType.MUTATION_MAPPING.qualifiedAnnotationName),
                    "value"
                )
            ).or(
                PsiJavaPatterns.psiElement().insideAnnotationParam(
                    StandardPatterns.string().equalTo(SchemaMappingType.MUTATION_MAPPING.qualifiedAnnotationName),
                    "name"
                )
            ).or(
                PsiJavaPatterns.psiElement().insideAnnotationParam(
                    StandardPatterns.string().equalTo(SchemaMappingType.SUBSCRIPTION_MAPPING.qualifiedAnnotationName),
                    "value"
                )
            ).or(
                PsiJavaPatterns.psiElement().insideAnnotationParam(
                    StandardPatterns.string().equalTo(SchemaMappingType.SUBSCRIPTION_MAPPING.qualifiedAnnotationName),
                    "name"
                )
            ),
            SchemaMappingFieldNameReferenceProvider()
        )
    }

}