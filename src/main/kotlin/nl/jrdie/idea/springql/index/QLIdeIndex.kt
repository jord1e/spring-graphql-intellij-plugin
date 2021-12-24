package nl.jrdie.idea.springql.index

import com.intellij.psi.PsiElement
import nl.jrdie.idea.springql.index.entry.QLClassBatchMappingIndexEntry
import nl.jrdie.idea.springql.index.entry.QLClassSchemaMappingIndexEntry
import nl.jrdie.idea.springql.index.entry.QLMethodBatchMappingIndexEntry
import nl.jrdie.idea.springql.index.entry.QLMethodSchemaMappingIndexEntry
import org.jetbrains.uast.UAnnotation

interface QLIdeIndex {

    fun schemaMappingByAnnotation(uAnnotation: UAnnotation): Set<QLMethodSchemaMappingIndexEntry>

    fun schemaMappingBySchemaPsi(psiElement: PsiElement): Set<QLMethodSchemaMappingIndexEntry>

    interface Builder<B : Builder<B>> {

        fun withMethodSchemaMapping(schemaMapping: QLMethodSchemaMappingIndexEntry): B

        fun withClassSchemaMapping(schemaMapping: QLClassSchemaMappingIndexEntry): B

        fun withMethodBatchMapping(batchMapping: QLMethodBatchMappingIndexEntry): B

        fun withClassBatchMapping(batchMapping: QLClassBatchMappingIndexEntry): B

        fun build(): QLIdeIndex

    }

}