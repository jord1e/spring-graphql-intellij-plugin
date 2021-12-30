package nl.jrdie.idea.springql.ide.quickfix

import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.openapi.project.Project
import nl.jrdie.idea.springql.utils.UClassAnnotatorUtil

class QLAddControllerAnnotationQuickFix : LocalQuickFix {

    override fun getFamilyName(): String = name

    override fun getName(): String = "Add @Controller annotation"

    override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
        UClassAnnotatorUtil.addAnnotation(descriptor.psiElement!!, "org.springframework.stereotype.Controller")
    }
}
