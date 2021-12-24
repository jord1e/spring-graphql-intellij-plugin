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

package nl.jrdie.idea.springql.ide.highlighting

import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.psi.PsiElement
import nl.jrdie.idea.springql.utils.QLIdeUtil
import org.jetbrains.uast.UMethod
import org.jetbrains.uast.toUElement

// TODO: Spring merges the annotations using their utility class
//   https://github.com/spring-projects/spring-graphql/blob/main/spring-graphql/src/main/java/org/springframework/graphql/data/method/annotation/support/AnnotatedControllerConfigurer.java#L206
//   Add quick fix to simplify
class DuplicateSchemaMappingAnnotator : Annotator {

    override fun annotate(psiElement: PsiElement, holder: AnnotationHolder) {
        val uElement = psiElement.toUElement();
        if (uElement !is UMethod) {
            return
        }

        val numSchemaMappingAnnotations = uElement
            .uAnnotations
            .filter(QLIdeUtil::isSchemaMappingAnnotation)

        if (numSchemaMappingAnnotations.size > 1) {
            numSchemaMappingAnnotations.forEach {
                holder
                    .newAnnotation(
                        HighlightSeverity.WEAK_WARNING,
                        "The mapping annotations will be reduced by Spring"
                    )
                    .range(it.sourcePsi!!)
                    .create()
            }
        }
    }
}