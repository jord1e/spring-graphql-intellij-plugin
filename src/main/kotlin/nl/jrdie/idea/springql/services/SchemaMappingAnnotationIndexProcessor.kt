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

package nl.jrdie.idea.springql.services

import com.intellij.lang.jsgraphql.types.language.ObjectTypeDefinition
import com.intellij.lang.jsgraphql.types.schema.idl.TypeDefinitionRegistry
import com.intellij.psi.PsiAnnotation
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiMethod
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.util.parentOfType
import com.intellij.util.Processor
import com.intellij.util.containers.orNull
import nl.jrdie.idea.springql.index.KaraIdeIndex
import nl.jrdie.idea.springql.index.SchemaMappingAnnotationIndexEntry
import nl.jrdie.idea.springql.utils.KaraIdeUtil
import org.jetbrains.uast.UAnnotation
import org.jetbrains.uast.UMethod
import org.jetbrains.uast.toUElementOfType

class SchemaMappingAnnotationIndexProcessor(
    private val annotationIndex: KaraIdeIndex,
    private val typeDefinitionRegistry: TypeDefinitionRegistry
) : Processor<PsiAnnotation> {

    override fun process(psiAnnotation: PsiAnnotation): Boolean {
        val psiMethod = psiAnnotation.parentOfType<PsiMethod>()
        val uMethod = psiMethod.toUElementOfType<UMethod>()!!

        val typeName = KaraIdeUtil.getSchemaMappingTypeName(uMethod)
        checkNotNull(typeName)
        val field = KaraIdeUtil.getGraphQlField(uMethod)
        checkNotNull(field)

        val graphQlType: PsiElement? = typeDefinitionRegistry
            .getType(typeName, ObjectTypeDefinition::class.java)
            .orNull()
            ?.fieldDefinitions
            ?.find { it.name == field }
            ?.sourceLocation
            ?.element

//                println("ABCD: " + graphQlType)

        val annotationIndexEntry = SchemaMappingAnnotationIndexEntry(
            typeName,
            field,
            uMethod,
            psiAnnotation.toUElementOfType()!!,
            graphQlType
        )

        println("Added ${typeName}.${field}")

//                println("ABCDE: " + annotationIndexEntry)
        annotationIndex.annotationIndex.add(annotationIndexEntry)

        return true
    }
}