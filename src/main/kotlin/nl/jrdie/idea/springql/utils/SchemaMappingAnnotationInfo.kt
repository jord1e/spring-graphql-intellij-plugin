package nl.jrdie.idea.springql.utils

import nl.jrdie.idea.springql.models.annotations.SchemaMappingType
import org.jetbrains.uast.UAnnotation

data class SchemaMappingAnnotationInfo(
    val uAnnotation: UAnnotation,
    val mappingType: SchemaMappingType
)
