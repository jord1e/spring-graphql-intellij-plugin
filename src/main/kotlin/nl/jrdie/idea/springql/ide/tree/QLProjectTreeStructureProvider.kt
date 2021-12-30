package nl.jrdie.idea.springql.ide.tree

import com.intellij.ide.projectView.PresentationData
import com.intellij.ide.projectView.ProjectViewNode
import com.intellij.ide.projectView.TreeStructureProvider
import com.intellij.ide.projectView.ViewSettings
import com.intellij.ide.util.treeView.AbstractTreeNode
import com.intellij.openapi.components.service
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.fileEditor.OpenFileDescriptor
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiElement
import com.intellij.ui.SimpleTextAttributes
import nl.jrdie.idea.springql.icons.QLIcons
import nl.jrdie.idea.springql.svc.QLIdeService

class QLProjectTreeStructureProvider : TreeStructureProvider {

    override fun modify(
        parent: AbstractTreeNode<*>,
        children: MutableCollection<AbstractTreeNode<*>>,
        settings: ViewSettings?
    ): MutableCollection<AbstractTreeNode<*>> {
        val svc = parent.project?.service<QLIdeService>()
        if (svc?.isApplicableProject(parent.project) == true && parent.parent == null /* Root Node */) {
            children.add(QLRootNode(svc, parent.project!!, settings))
        }

        return children
    }
}

class QLRootNode(
    private val ideService: QLIdeService,
    myProject: Project,
    settings: ViewSettings?
) : ProjectViewNode<String>(myProject, "", settings) {

    @Suppress("DialogTitleCapitalization")
    override fun update(presentation: PresentationData) {
        presentation.addText("Spring GraphQL", SimpleTextAttributes.REGULAR_ATTRIBUTES)
        presentation.setIcon(QLIcons.SpringGraphQL)
    }

    override fun getChildren(): MutableCollection<out AbstractTreeNode<*>> {
        val list = mutableListOf<AbstractTreeNode<*>>()
        list.add(QLSchemaMappingRootNode(ideService, myProject, "Data Fetchers", settings))
        list.add(QLSchemaMappingRootNode(ideService, myProject, "Batch Loaders", settings))
        return list
    }

    override fun contains(file: VirtualFile) = false
}

class QLSchemaMappingRootNode(
    private val ideService: QLIdeService,
    myProject: Project,
    private val text: String,
    settings: ViewSettings?
) : ProjectViewNode<String>(myProject, text, settings) {

    override fun update(presentation: PresentationData) {
        presentation.addText(text, SimpleTextAttributes.REGULAR_ATTRIBUTES)
        presentation.setIcon(QLIcons.SchemaMappingMethod)
    }

    override fun getChildren(): MutableCollection<out AbstractTreeNode<*>> {
        return ideService.index.allMethodSchemaMappingEntries()
            .map { QLSchemaMappingNode(myProject, "${it.parentType}.${it.field}", it.methodPsi, settings) }
            .toMutableList()
    }

    override fun contains(file: VirtualFile) = false
}

class QLSchemaMappingNode(
    project: Project,
    private val schemaLocation: String,
    private val annotationElement: PsiElement,
    viewSettings: ViewSettings?
) : ProjectViewNode<String>(project, schemaLocation, viewSettings) {

    override fun update(presentation: PresentationData) {
        presentation.addText(schemaLocation, SimpleTextAttributes.REGULAR_ATTRIBUTES)
        presentation.setIcon(QLIcons.SchemaMappingMethod)
    }

    override fun getChildren(): MutableCollection<out AbstractTreeNode<*>> = mutableListOf()

    override fun contains(file: VirtualFile) = false

    override fun navigate(requestFocus: Boolean) {
        FileEditorManager.getInstance(annotationElement.project)
            .openEditor(
                OpenFileDescriptor(
                    annotationElement.project,
                    annotationElement.containingFile.virtualFile,
                    annotationElement.textOffset
                ),
                true
            )
    }

    override fun canNavigate() = true

    override fun canNavigateToSource() = true

    override fun isAlwaysLeaf() = true
}
