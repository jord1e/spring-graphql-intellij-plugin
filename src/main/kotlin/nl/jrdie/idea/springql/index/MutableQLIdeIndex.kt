package nl.jrdie.idea.springql.index

import com.intellij.psi.PsiElement
import com.intellij.util.containers.filterSmart
import nl.jrdie.idea.springql.index.entry.QLClassSchemaMappingIndexEntry
import nl.jrdie.idea.springql.index.entry.QLMethodBatchMappingIndexEntry
import nl.jrdie.idea.springql.index.entry.QLMethodSchemaMappingIndexEntry
import nl.jrdie.idea.springql.index.entry.SchemaMappingIndexEntry
import org.jetbrains.uast.UAnnotation
import org.jetbrains.uast.UClass

class MutableQLIdeIndex(
    private val methodSchemaMappingEntries: Set<QLMethodSchemaMappingIndexEntry>,
    private val classSchemaMappingEntries: Set<QLClassSchemaMappingIndexEntry>,
    private val methodBatchMappingEntries: Set<QLMethodBatchMappingIndexEntry>,
) : QLIdeIndex {

    override fun schemaMappingByAnnotation(uAnnotation: UAnnotation): Set<SchemaMappingIndexEntry> {
        return methodSchemaMappingEntries
            .filterSmart { it.annotationPsi == uAnnotation.sourcePsi }
            .toSet() union classSchemaMappingEntries
            .filterSmart { it.annotationPsi == uAnnotation.sourcePsi }
            .toSet()
    }

    override fun methodSchemaMappingByAnnotation(uAnnotation: UAnnotation): Set<QLMethodSchemaMappingIndexEntry> {
        return methodSchemaMappingEntries
            .filterSmart { it.annotationPsi == uAnnotation.sourcePsi }
            .toSet()
    }

    override fun schemaMappingByClass(uClass: UClass): Set<QLClassSchemaMappingIndexEntry> {
        return classSchemaMappingEntries
            .filterSmart { it.classPsi == uClass.sourcePsi }
            .toSet()
    }

    override fun schemaMappingBySchemaPsi(psiElement: PsiElement): Set<QLMethodSchemaMappingIndexEntry> {
        return methodSchemaMappingEntries
            .filterSmart { it.schemaPsi.contains(psiElement) }
            .toSet()
    }

    override fun allMethodSchemaMappingEntries(): Set<QLMethodSchemaMappingIndexEntry> {
        return methodSchemaMappingEntries
    }

    override fun allMethodBatchMappingEntries(): Set<QLMethodBatchMappingIndexEntry> {
        return methodBatchMappingEntries
    }

    class MutableQLIdeIndexBuilder : QLIdeIndex.Builder<MutableQLIdeIndexBuilder> {
        private val methodSchemaMappingEntries: MutableSet<QLMethodSchemaMappingIndexEntry> = mutableSetOf()
        private val classSchemaMappingEntries: MutableSet<QLClassSchemaMappingIndexEntry> = mutableSetOf()
        private val methodBatchMappingEntries: MutableSet<QLMethodBatchMappingIndexEntry> = mutableSetOf()

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

        override fun build(): QLIdeIndex {
            return MutableQLIdeIndex(
                methodSchemaMappingEntries,
                classSchemaMappingEntries,
                methodBatchMappingEntries
            )
        }
    }
}
