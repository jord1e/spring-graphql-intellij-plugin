package nl.jrdie.idea.springql.ide.quickfix

import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.openapi.project.Project
import nl.jrdie.idea.springql.utils.UClassAnnotatorUtil
import org.jetbrains.uast.UClass
import org.jetbrains.uast.getUastParentOfType

class QLAddControllerAnnotationQuickFix : LocalQuickFix {

    override fun getFamilyName(): String = name

    override fun getName(): String = "Add @Controller annotation"

    override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
        val sourcePsi = descriptor.psiElement?.getUastParentOfType<UClass>()?.sourcePsi
        if (sourcePsi != null) {
            UClassAnnotatorUtil.addAnnotation(sourcePsi, "org.springframework.stereotype.Controller")
        }
    }
}
