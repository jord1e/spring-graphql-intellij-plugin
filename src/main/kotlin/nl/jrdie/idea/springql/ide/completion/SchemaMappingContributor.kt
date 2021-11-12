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