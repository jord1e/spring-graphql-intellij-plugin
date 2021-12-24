package nl.jrdie.idea.springql.index

import com.intellij.psi.PsiElement
import com.intellij.util.containers.filterSmart
import nl.jrdie.idea.springql.index.entry.QLClassBatchMappingIndexEntry
import nl.jrdie.idea.springql.index.entry.QLClassSchemaMappingIndexEntry
import nl.jrdie.idea.springql.index.entry.QLMethodBatchMappingIndexEntry
import nl.jrdie.idea.springql.index.entry.QLMethodSchemaMappingIndexEntry
import org.jetbrains.uast.UAnnotation

class MutableQLIdeIndex(
    private val methodSchemaMappingEntries: MutableSet<QLMethodSchemaMappingIndexEntry>,
    private val classSchemaMappingEntries: MutableSet<QLClassSchemaMappingIndexEntry>,
    private val methodBatchMappingEntries: MutableSet<QLMethodBatchMappingIndexEntry>,
    private val classBatchMappingEntries: MutableSet<QLClassBatchMappingIndexEntry>
) : QLIdeIndex {

    override fun schemaMappingByAnnotation(uAnnotation: UAnnotation): Set<QLMethodSchemaMappingIndexEntry> {
        return methodSchemaMappingEntries
            .filterSmart { it.annotationPsi == uAnnotation.sourcePsi }
            .toSet()
    }

    override fun schemaMappingBySchemaPsi(psiElement: PsiElement): Set<QLMethodSchemaMappingIndexEntry> {
        return methodSchemaMappingEntries
            .filterSmart { it.schemaPsi.contains(psiElement) }
            .toSet()
    }

    class MutableQLIdeIndexBuilder : QLIdeIndex.Builder<MutableQLIdeIndexBuilder> {
        private val methodSchemaMappingEntries: MutableSet<QLMethodSchemaMappingIndexEntry> = mutableSetOf()
        private val classSchemaMappingEntries: MutableSet<QLClassSchemaMappingIndexEntry> = mutableSetOf()
        private val methodBatchMappingEntries: MutableSet<QLMethodBatchMappingIndexEntry> = mutableSetOf()
        private val classBatchMappingEntries: MutableSet<QLClassBatchMappingIndexEntry> = mutableSetOf()

        override fun withMethodSchemaMapping(schemaMapping: QLMethodSchemaMappingIndexEntry): MutableQLIdeIndexBuilder {
            methodSchemaMappingEntries.add(schemaMapping)
            return this
        }

        override fun withClassSchemaMapping(schemaMapping: QLClassSchemaMappingIndexEntry): MutableQLIdeIndexBuilder {
            classSchemaMappingEntries.add(schemaMapping)
            return this
        }

        override fun withMethodBatchMapping(batchMapping: QLMethodBatchMappingIndexEntry): MutableQLIdeIndexBuilder {
            methodBatchMappingEntries.add(batchMapping)
            return this
        }

        override fun withClassBatchMapping(batchMapping: QLClassBatchMappingIndexEntry): MutableQLIdeIndexBuilder {
            classBatchMappingEntries.add(batchMapping)
            return this
        }

        override fun build(): QLIdeIndex {
            return MutableQLIdeIndex(
                methodSchemaMappingEntries,
                classSchemaMappingEntries,
                methodBatchMappingEntries,
                classBatchMappingEntries
            )
        }
    }

}