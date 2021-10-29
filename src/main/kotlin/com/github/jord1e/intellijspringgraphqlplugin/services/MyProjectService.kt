package com.github.jord1e.intellijspringgraphqlplugin.services

import com.intellij.openapi.project.Project
import com.github.jord1e.intellijspringgraphqlplugin.MyBundle

class MyProjectService(project: Project) {

    init {
        println(MyBundle.message("projectService", project.name))
    }
}
