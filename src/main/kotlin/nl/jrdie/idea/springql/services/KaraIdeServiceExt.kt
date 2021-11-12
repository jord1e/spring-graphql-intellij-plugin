package nl.jrdie.idea.springql.services

import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project

fun Project.getKaraService(): KaraIdeService {
    return this.service()
}
