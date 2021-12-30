package nl.jrdie.idea.springql.utils

import org.jetbrains.uast.UAnnotated

fun UAnnotated.hasUAnnotation(fqName: String): Boolean {
    return this.uAnnotations
        .any { it.qualifiedName == fqName }
}
