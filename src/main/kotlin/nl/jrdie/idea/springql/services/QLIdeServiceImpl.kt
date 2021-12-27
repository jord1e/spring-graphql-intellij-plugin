// /*
// * Copyright (C) 2021 Jordie
// *
// * This program is free software: you can redistribute it and/or modify
// * it under the terms of the GNU General Public License as published by
// * the Free Software Foundation, either version 3 of the License, or
// * (at your option) any later version.
// *
// * This program is distributed in the hope that it will be useful,
// * but WITHOUT ANY WARRANTY; without even the implied warranty of
// * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// * GNU General Public License for more details.
// *
// * You should have received a copy of the GNU General Public License
// * along with this program.  If not, see <https://www.gnu.org/licenses/>.
// */
//
// package nl.jrdie.idea.springql.services
//
// import com.intellij.AppTopics
// import com.intellij.ide.highlighter.JavaFileType
// import com.intellij.lang.jsgraphql.schema.GraphQLSchemaChangeListener
// import com.intellij.lang.jsgraphql.schema.GraphQLSchemaEventListener
// import com.intellij.lang.jsgraphql.schema.GraphQLSchemaInfo
// import com.intellij.lang.jsgraphql.schema.GraphQLSchemaProvider
// import com.intellij.lang.jsgraphql.types.schema.idl.TypeDefinitionRegistry
// import com.intellij.openapi.Disposable
// import com.intellij.openapi.editor.Document
// import com.intellij.openapi.fileEditor.FileDocumentManager
// import com.intellij.openapi.fileEditor.FileDocumentManagerListener
// import com.intellij.openapi.project.Project
// import com.intellij.psi.PsiManager
// import com.intellij.psi.impl.java.stubs.index.JavaStubIndexKeys
// import com.intellij.psi.search.GlobalSearchScope
// import com.intellij.psi.stubs.StubIndex
// import nl.jrdie.idea.springql.models.annotations.SchemaMappingType
// import org.jetbrains.kotlin.idea.KotlinFileType
//
// class QLIdeServiceImpl(
//    private val project: Project
// ) : Disposable {
//
//    private var annotationIndex: QLIdeIndex2? = null
//
//    fun getAnnotationIndex(): QLIdeIndex2 {
//        val aidx = annotationIndex
//        if (aidx != null) {
//            return aidx
//        }
//
//        val theNewIndex = QLIdeIndex2()
//        annotationIndex = theNewIndex
//
//        val typeDefinitionRegistry = getTypeDefinitionRegistry(project)
//        val annotationIndexProcessor = QLAnnotationIndexProcessor(typeDefinitionRegistry)
//
//        theNewIndex.annotationIndex.clear(); // TODO
//
//        val stubIndex = StubIndex.getInstance()
//
//        for (value in SchemaMappingType.values()) {
//            stubIndex.processElements(
//                JavaStubIndexKeys.ANNOTATIONS,
//                value.simpleName,
//                project,
//                GlobalSearchScope.projectScope(project),
//                UAN::class.java,
//                annotationIndexProcessor
//            )
//        }
//
//        project.messageBus.connect(this).subscribe(
//            GraphQLSchemaEventListener.TOPIC,
//            GraphQLSchemaChangeListener {
//                annotationIndex = null
// //                theNewIndex.annotationIndex.clear()
//            }
//        )
//
//        project.messageBus.connect(this).subscribe(
//            AppTopics.FILE_DOCUMENT_SYNC,
//            object : FileDocumentManagerListener {
//                override fun beforeDocumentSaving(document: Document) {
//                    val file = FileDocumentManager.getInstance().getFile(document)
//
//                    when (file?.fileType) {
//                        JavaFileType.INSTANCE,
//                        KotlinFileType.INSTANCE -> {
//                            annotationIndex = null;
// //                            theNewIndex.annotationIndex.clear()
//                        }
//                    }
//                }
//            }
//        )
//
// //        FileTypeIndex.processFiles(
// //            JavaFileType.INSTANCE,
// //            { file ->
// //                val psiFile = psiManager.findFile(file)
// //                if (psiFile != null) {
// //                    annotationIndexProcessor.process(psiFile)
// //                }
// //                true
// //            },
// //            GlobalSearchScope.getScopeRestrictedByFileTypes(
// //                GlobalSearchScope.projectScope(project),
// //                JavaFileType.INSTANCE
// //            )
// //        )
//
// //        FileTypeIndex.processFiles(
// //            KotlinFileType.INSTANCE,
// //            { file ->
// //                val psiFile = psiManager.findFile(file)
// //                if (psiFile != null) {
// //                    annotationIndexProcessor.process(psiFile)
// //                }
// //                true
// //            },
// //            GlobalSearchScope.getScopeRestrictedByFileTypes(
// //                GlobalSearchScope.projectScope(project),
// //                KotlinFileType.INSTANCE
// //            )
// //        )
//
//        return theNewIndex
//    }
//
//    fun getTypeDefinitionRegistry(project: Project): TypeDefinitionRegistry {
//        return GraphQLSchemaProvider
//            .getInstance(project)
//            .getRegistryInfo(PsiManager.getInstance(project).findFile(project.projectFile!!)!!)
//            .typeDefinitionRegistry
//    }
//
//    fun getGraphQLSchemaInfo(project: Project): GraphQLSchemaInfo {
//        val typeDefinitionRegistry = GraphQLSchemaProvider.getInstance(project)
//        return GraphQLSchemaProvider
//            .getInstance(project)
//            .getSchemaInfo(PsiManager.getInstance(project).findFile(project.projectFile!!)!!)
//    }
//
//    override fun dispose() {
//        TODO("Not yet implemented")
//    }
//
// }
