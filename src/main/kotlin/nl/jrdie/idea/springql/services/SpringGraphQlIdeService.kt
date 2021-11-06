package nl.jrdie.idea.springql.services

import com.intellij.ide.highlighter.JavaFileType
import com.intellij.lang.jsgraphql.schema.GraphQLSchemaInfo
import com.intellij.lang.jsgraphql.schema.GraphQLSchemaProvider
import com.intellij.lang.jsgraphql.types.schema.idl.TypeDefinitionRegistry
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiManager
import com.intellij.psi.search.FileTypeIndex
import com.intellij.psi.search.GlobalSearchScope
import nl.jrdie.idea.springql.index.SpringGraphQlIdeIndex
import org.jetbrains.kotlin.idea.extensions.gradle.getTopLevelBuildScriptPsiFile

class SpringGraphQlIdeService(
    private val project: Project
) {

    private val annotationIndex: SpringGraphQlIdeIndex

    init {
        annotationIndex = SpringGraphQlIdeIndex();
    }

    fun getAnnotationIndex(): SpringGraphQlIdeIndex {
        val psiManager = PsiManager.getInstance(project)
        val typeDefinitionRegistry = getTypeDefinitionRegistry(project)
        val annotationIndexProcessor = SchemaMappingAnnotationIndexProcessor(annotationIndex, typeDefinitionRegistry)

        annotationIndex.annotationIndex.clear(); // TODO

        FileTypeIndex.processFiles(
            JavaFileType.INSTANCE,
            { file ->
                val psiFile = psiManager.findFile(file)
                if (psiFile != null) {
                    annotationIndexProcessor.process(psiFile)
                }
                true
            },
            GlobalSearchScope.getScopeRestrictedByFileTypes(
                GlobalSearchScope.projectScope(project),
                JavaFileType.INSTANCE
            )
        )

//        FileTypeIndex.processFiles(
//            KotlinFileType.INSTANCE,
//            { file ->
//                val psiFile = psiManager.findFile(file)
//                if (psiFile != null) {
//                    annotationIndexProcessor.process(psiFile)
//                }
//                true
//            },
//            GlobalSearchScope.getScopeRestrictedByFileTypes(
//                GlobalSearchScope.projectScope(project),
//                KotlinFileType.INSTANCE
//            )
//        )

        return annotationIndex
    }

    fun getTypeDefinitionRegistry(project: Project): TypeDefinitionRegistry {
        return GraphQLSchemaProvider
            .getInstance(project)
            .getRegistryInfo(project.getTopLevelBuildScriptPsiFile()!!)
            .typeDefinitionRegistry
    }

    fun getGraphQLSchemaInfo(project: Project): GraphQLSchemaInfo {
        val typeDefinitionRegistry = GraphQLSchemaProvider.getInstance(project)
        return GraphQLSchemaProvider
            .getInstance(project)
            .getSchemaInfo(project.getTopLevelBuildScriptPsiFile()!!)
    }

}

fun Project.getGraphQlIdeService(): SpringGraphQlIdeService {
    return this.service()
}
