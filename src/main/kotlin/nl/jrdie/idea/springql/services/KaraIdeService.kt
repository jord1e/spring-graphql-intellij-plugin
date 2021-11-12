package nl.jrdie.idea.springql.services

import com.intellij.AppTopics
import com.intellij.ide.highlighter.JavaFileType
import com.intellij.lang.jsgraphql.schema.GraphQLSchemaChangeListener
import com.intellij.lang.jsgraphql.schema.GraphQLSchemaEventListener
import com.intellij.lang.jsgraphql.schema.GraphQLSchemaInfo
import com.intellij.lang.jsgraphql.schema.GraphQLSchemaProvider
import com.intellij.lang.jsgraphql.types.schema.idl.TypeDefinitionRegistry
import com.intellij.openapi.Disposable
import com.intellij.openapi.editor.Document
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.fileEditor.FileDocumentManagerListener
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiAnnotation
import com.intellij.psi.PsiManager
import com.intellij.psi.impl.java.stubs.index.JavaStubIndexKeys
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.stubs.StubIndex
import nl.jrdie.idea.springql.index.KaraIdeIndex
import nl.jrdie.idea.springql.models.annotations.SchemaMappingType
import org.jetbrains.kotlin.idea.KotlinFileType

class KaraIdeService(
    private val project: Project
) : Disposable {

    private var annotationIndex: KaraIdeIndex? = null

    fun getAnnotationIndex(): KaraIdeIndex {
        val aidx = annotationIndex
        if (aidx != null) {
            return aidx
        }

        val theNewIndex = KaraIdeIndex()
        annotationIndex = theNewIndex

        val typeDefinitionRegistry = getTypeDefinitionRegistry(project)
        val annotationIndexProcessor = SchemaMappingAnnotationIndexProcessor(theNewIndex, typeDefinitionRegistry)

        theNewIndex.annotationIndex.clear(); // TODO

        val stubIndex = StubIndex.getInstance()

        for (value in SchemaMappingType.values()) {
            stubIndex.processElements(
                JavaStubIndexKeys.ANNOTATIONS,
                value.simpleName,
                project,
                GlobalSearchScope.projectScope(project),
                PsiAnnotation::class.java,
                annotationIndexProcessor
            )
        }

        project.messageBus.connect(this).subscribe(
            GraphQLSchemaChangeListener.TOPIC,
            GraphQLSchemaEventListener {
                annotationIndex = null
//                theNewIndex.annotationIndex.clear()
            }
        )

        project.messageBus.connect(this).subscribe(
            AppTopics.FILE_DOCUMENT_SYNC,
            object : FileDocumentManagerListener {
                override fun beforeDocumentSaving(document: Document) {
                    val file = FileDocumentManager.getInstance().getFile(document)

                    when (file?.fileType) {
                        JavaFileType.INSTANCE,
                        KotlinFileType.INSTANCE -> {
                            annotationIndex = null;
//                            theNewIndex.annotationIndex.clear()
                        }
                    }
                }
            }
        )

//        FileTypeIndex.processFiles(
//            JavaFileType.INSTANCE,
//            { file ->
//                val psiFile = psiManager.findFile(file)
//                if (psiFile != null) {
//                    annotationIndexProcessor.process(psiFile)
//                }
//                true
//            },
//            GlobalSearchScope.getScopeRestrictedByFileTypes(
//                GlobalSearchScope.projectScope(project),
//                JavaFileType.INSTANCE
//            )
//        )

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

        return theNewIndex
    }

    fun getTypeDefinitionRegistry(project: Project): TypeDefinitionRegistry {
        return GraphQLSchemaProvider
            .getInstance(project)
            .getRegistryInfo(PsiManager.getInstance(project).findFile(project.projectFile!!)!!)
            .typeDefinitionRegistry
    }

    fun getGraphQLSchemaInfo(project: Project): GraphQLSchemaInfo {
        val typeDefinitionRegistry = GraphQLSchemaProvider.getInstance(project)
        return GraphQLSchemaProvider
            .getInstance(project)
            .getSchemaInfo(PsiManager.getInstance(project).findFile(project.projectFile!!)!!)
    }

    override fun dispose() {
        TODO("Not yet implemented")
    }

}
