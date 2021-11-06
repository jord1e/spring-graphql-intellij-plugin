package nl.jrdie.idea.springql.ide.completion

import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReference
import com.intellij.psi.PsiReferenceProvider
import com.intellij.util.ProcessingContext
import nl.jrdie.idea.springql.ide.GraphQlTypeNameReference
import org.jetbrains.uast.ULiteralExpression
import org.jetbrains.uast.UNamedExpression
import org.jetbrains.uast.getParentOfType
import org.jetbrains.uast.kotlin.KotlinStringULiteralExpression
import org.jetbrains.uast.toUElement

class SchemaMappingAnnotationGraphQlReferenceProvider : PsiReferenceProvider() {

//    override fun registerReferenceProviders(registrar: PsiReferenceRegistrar) {
//        registrar.registerUastReferenceProvider({ uElement, _ -> uElement is ULiteralExpression }, ReferenceProvider)
////        registrar.registerReferenceProvider(PlatformPatterns.psiElement(PsiLiteralExpression::class.java), ReferenceProvider)
//    }

//    private object ReferenceProvider : PsiReferenceProvider() {
//        override fun getReferencesByElement(element: PsiElement, context: ProcessingContext): Array<PsiReference> {
//            val project = element.project
//            val graphqlService = project.service<SpringGraphQlIdeService>()
//            println("111:" + element.toUElement()?.javaClass?.name)
//
//            return arrayOf<PsiReference>(GraphQlTypeNameReference(element))
//        }
//
//    }
//
//    private object ReferenceProvider : UastReferenceProvider(ULiteralExpression::class.java) {
//        override fun getReferencesByElement(element: UElement, context: ProcessingContext): Array<PsiReference> {
////            println("222:" + element?.uastParent?.javaClass?.name)
//
//    }

    override fun getReferencesByElement(element: PsiElement, context: ProcessingContext): Array<PsiReference> {
        val uElement = element.toUElement()
        if (uElement !is ULiteralExpression) {
            throw IllegalStateException("Should not happen")
//                return PsiReference.EMPTY_ARRAY
        }

//            println("5555: " + element?.uastParent?.sourcePsi?.javaClass?.name)
        if (uElement is KotlinStringULiteralExpression) {
            return PsiReference.EMPTY_ARRAY
        }

        val attribute = uElement.getParentOfType<UNamedExpression>()?.name
        if (attribute != "typeName") {
            return PsiReference.EMPTY_ARRAY
        }

        val project = uElement.sourcePsi?.project
        if (project == null) {
            return PsiReference.EMPTY_ARRAY
        }

        return arrayOf(GraphQlTypeNameReference(uElement.sourcePsi!!))
    }

}