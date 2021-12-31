package nl.jrdie.idea.springql.ide.inspection

import com.intellij.codeInspection.AbstractBaseUastLocalInspectionTool
import com.intellij.codeInspection.InspectionManager
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.openapi.components.service
import com.intellij.psi.PsiClass
import nl.jrdie.idea.springql.ide.quickfix.QLAddControllerAnnotationQuickFix
import nl.jrdie.idea.springql.svc.QLIdeService
import org.jetbrains.uast.UClass

@Suppress("FoldInitializerAndIfToElvis")
class QLMissingControllerAnnotationInspection : AbstractBaseUastLocalInspectionTool(UClass::class.java) {

    override fun checkClass(
        uClass: UClass,
        manager: InspectionManager,
        isOnTheFly: Boolean
    ): Array<ProblemDescriptor>? {
        val sourcePsi = uClass.sourcePsi
        if (sourcePsi !is PsiClass) {
            return null
        }

        val project = sourcePsi.project

        val svc = project.service<QLIdeService>()
        if (!svc.isApplicableProject(project)) {
            return null
        }

        if (!svc.needsControllerAnnotation(uClass)) {
            return null
        }

        val problemDescriptor = manager.createProblemDescriptor(
            sourcePsi.identifyingElement!!,
            "Add @Controller annotation",
            QLAddControllerAnnotationQuickFix(),
            ProblemHighlightType.WARNING,
            isOnTheFly
        )

        return arrayOf(problemDescriptor)
    }
}
