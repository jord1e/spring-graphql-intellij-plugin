package nl.jrdie.idea.springql.index

import com.intellij.psi.PsiElement
import nl.jrdie.idea.springql.index.entry.QLClassSchemaMappingIndexEntry
import nl.jrdie.idea.springql.index.entry.QLMethodBatchMappingIndexEntry
import nl.jrdie.idea.springql.index.entry.QLMethodSchemaMappingIndexEntry
import nl.jrdie.idea.springql.index.entry.SchemaMappingIndexEntry
import org.jetbrains.uast.UAnnotation
import org.jetbrains.uast.UClass
import org.jetbrains.uast.UMethod

interface QLIdeIndex {

    fun schemaMappingByAnnotation(uAnnotation: UAnnotation): List<SchemaMappingIndexEntry>

    fun methodSchemaMappingByAnnotation(uAnnotation: UAnnotation): List<QLMethodSchemaMappingIndexEntry>

    fun methodSchemaMappingByMethod(uMethod: UMethod): List<QLMethodSchemaMappingIndexEntry>

    fun schemaMappingByClass(uClass: UClass): List<QLClassSchemaMappingIndexEntry>

    fun schemaMappingBySchemaPsi(psiElement: PsiElement): List<QLMethodSchemaMappingIndexEntry>

    fun allMethodSchemaMappingEntries(): List<QLMethodSchemaMappingIndexEntry>

    fun allMethodBatchMappingEntries(): List<QLMethodBatchMappingIndexEntry>

    fun schemaMappingByMethod(uMethod: UMethod): List<SchemaMappingIndexEntry>

    interface Builder<B : Builder<B>> {

        fun withMethodSchemaMapping(schemaMapping: QLMethodSchemaMappingIndexEntry): B

        fun withClassSchemaMapping(schemaMapping: QLClassSchemaMappingIndexEntry): B

        fun withMethodBatchMapping(batchMapping: QLMethodBatchMappingIndexEntry): B

        fun build(): QLIdeIndex
    }
}
