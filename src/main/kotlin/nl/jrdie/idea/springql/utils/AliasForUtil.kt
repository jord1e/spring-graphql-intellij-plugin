package nl.jrdie.idea.springql.utils

import org.jetbrains.uast.UAnnotation
import org.jetbrains.uast.evaluateString

object AliasForUtil {

    fun findValue(uAnnotation: UAnnotation, attribute: String): String? {
        return uAnnotation
            .findAttributeValue(attribute)
            ?.evaluateString()
    }
}
