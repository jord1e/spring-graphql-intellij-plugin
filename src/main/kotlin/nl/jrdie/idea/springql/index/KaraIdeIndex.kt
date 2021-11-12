package nl.jrdie.idea.springql.index

import com.intellij.lang.jsgraphql.psi.GraphQLFieldDefinition
import org.jetbrains.uast.UAnnotation
import org.jetbrains.uast.UMethod

class KaraIdeIndex {

    val annotationIndex: MutableSet<SchemaMappingAnnotationIndexEntry>

    init {
        annotationIndex = mutableSetOf();
    }

    fun findMappingsByMethod(psiMethod: UMethod): List<SchemaMappingAnnotationIndexEntry> {
        return annotationIndex.filter { it.dataFetcherMethod == psiMethod }
    }

    fun findMappingsByAnnotation(psiAnnotation: UAnnotation): List<SchemaMappingAnnotationIndexEntry> {
        return annotationIndex.filter { it.annotation == psiAnnotation }
    }

    fun findMappingsByFieldDefinition(graphQLFieldDefinition: GraphQLFieldDefinition): List<SchemaMappingAnnotationIndexEntry> {
        return annotationIndex.filter { it.graphQlSchemaPsi == graphQLFieldDefinition }
    }

}