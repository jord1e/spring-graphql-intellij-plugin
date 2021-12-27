package nl.jrdie.idea.springql.utils

import com.intellij.psi.JavaPsiFacade
import com.intellij.psi.PsiAnnotation
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementFactory
import com.intellij.psi.PsiJavaFile
import org.jetbrains.kotlin.idea.util.addAnnotation
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.psi.KtClass

object UClassAnnotatorUtil {

    // TODO Better solution
    fun addAnnotation(sourcePsi: PsiElement, fqName: String): Boolean {
        // Java
        if (sourcePsi is PsiClass) {
            val factory: PsiElementFactory = JavaPsiFacade.getInstance(sourcePsi.project).elementFactory
            // Convert a.b.c.D.Abc to @D.Abc
            val theAnnotation = "@" + fqName.split('.').dropWhile { it.firstOrNull()?.isLowerCase() ?: true }
                .joinToString(separator = ".")
            val annotationFromText: PsiAnnotation = factory.createAnnotationFromText(theAnnotation, null)
            val firstModifier = sourcePsi.modifierList?.firstChild
            if (firstModifier != null) {
                sourcePsi.modifierList?.addBefore(annotationFromText, firstModifier)
            } else {
                sourcePsi.addBefore(annotationFromText, sourcePsi)
            }
            val importStatement = factory.createImportStatement(factory.createTypeByFQClassName(fqName).resolve()!!)
            (sourcePsi.containingFile as? PsiJavaFile)?.importList?.add(importStatement)
            return true
        }

        // Kotlin
        if (sourcePsi is KtClass) {
            val kotlinAnnotation = FqName(fqName)
            sourcePsi.addAnnotation(kotlinAnnotation)
            return true
        }

        throw IllegalArgumentException("Can not apply annotation '$fqName' to element $sourcePsi (FQN: ${sourcePsi::class.qualifiedName})")
    }

}