package nl.jrdie.idea.springql.services

import com.intellij.lang.jsgraphql.types.language.ObjectTypeDefinition
import com.intellij.lang.jsgraphql.types.schema.idl.TypeDefinitionRegistry
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.util.Processor
import com.intellij.util.containers.orNull
import nl.jrdie.idea.springql.index.SpringGraphQlIdeIndex
import nl.jrdie.idea.springql.index.SchemaMappingAnnotationIndexEntry
import nl.jrdie.idea.springql.utils.SpringGraphQlIdeUtil
import org.jetbrains.uast.UFile
import org.jetbrains.uast.UMethod
import org.jetbrains.uast.toUElementOfType

class SchemaMappingAnnotationIndexProcessor(
    private val annotationIndex: SpringGraphQlIdeIndex,
    private val typeDefinitionRegistry: TypeDefinitionRegistry
) : Processor<PsiFile> {

    override fun process(file: PsiFile): Boolean {
        file.toUElementOfType<UFile>()
        PsiTreeUtil
            .findChildrenOfType(file, PsiElement::class.java)
            .filter(SpringGraphQlIdeUtil::isSchemaMappingMethod)
            .map { method -> method.toUElementOfType<UMethod>()!! }
            .forEach { method ->
                val typeName = SpringGraphQlIdeUtil.getSchemaMappingTypeName(method)
                val fieldName = SpringGraphQlIdeUtil.getGraphQlField(method)
                val graphQlType: PsiElement? = typeDefinitionRegistry
                    .getType(typeName, ObjectTypeDefinition::class.java)
                    .orNull()
                    ?.fieldDefinitions
                    ?.find { it.name == fieldName }
                    ?.sourceLocation
                    ?.element

//                println("ABCD: " + graphQlType)

                val annotationIndexEntry = SchemaMappingAnnotationIndexEntry(
                    method,
                    SpringGraphQlIdeUtil.getSchemaMappingAnnotationInfo(method).first()!!.uAnnotation,
                    graphQlType
                )

//                println("ABCDE: " + annotationIndexEntry)
                annotationIndex.annotationIndex.add(annotationIndexEntry)
            }
        return true
    }
}