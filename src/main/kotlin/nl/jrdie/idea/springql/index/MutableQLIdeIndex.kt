package nl.jrdie.idea.springql.index

import com.intellij.psi.PsiElement
import com.intellij.psi.PsiManager
import com.intellij.util.containers.filterSmart
import nl.jrdie.idea.springql.index.entry.QLClassSchemaMappingIndexEntry
import nl.jrdie.idea.springql.index.entry.QLMethodBatchMappingIndexEntry
import nl.jrdie.idea.springql.index.entry.QLMethodSchemaMappingIndexEntry
import nl.jrdie.idea.springql.index.entry.SchemaMappingIndexEntry
import org.jetbrains.uast.UAnnotation
import org.jetbrains.uast.UClass
import org.jetbrains.uast.UMethod

@Suppress("FoldInitializerAndIfToElvis")
class MutableQLIdeIndex(
    private val methodSchemaMappingEntries: Set<QLMethodSchemaMappingIndexEntry>,
    private val classSchemaMappingEntries: Set<QLClassSchemaMappingIndexEntry>,
    private val methodBatchMappingEntries: Set<QLMethodBatchMappingIndexEntry>,
) : QLIdeIndex {

    override fun schemaMappingByAnnotation(uAnnotation: UAnnotation): List<SchemaMappingIndexEntry> {
        return (
            methodSchemaMappingByAnnotation(uAnnotation) union classSchemaMappingEntries
                .filterSmart { it.annotationPsi == uAnnotation.sourcePsi }
                .toList()
            ).toList()
    }

    override fun methodSchemaMappingByAnnotation(uAnnotation: UAnnotation): List<QLMethodSchemaMappingIndexEntry> {
        return methodSchemaMappingEntries
            .filterSmart {
                PsiManager.getInstance(uAnnotation.sourcePsi!!.project)
                    .areElementsEquivalent(it.annotationPsi, uAnnotation.sourcePsi)
            }
            .toList()
    }

    override fun methodSchemaMappingByMethod(uMethod: UMethod): List<QLMethodSchemaMappingIndexEntry> {
        return methodSchemaMappingEntries
            .filterSmart {
                PsiManager.getInstance(it.methodPsi.project).areElementsEquivalent(it.methodPsi, uMethod.sourcePsi)
            }
            .toList()
    }

    override fun schemaMappingByClass(uClass: UClass): List<QLClassSchemaMappingIndexEntry> {
        return classSchemaMappingEntries
            .filterSmart { it.classPsi == uClass.sourcePsi }
            .toList()
    }

    override fun schemaMappingBySchemaPsi(psiElement: PsiElement): List<QLMethodSchemaMappingIndexEntry> {
        return methodSchemaMappingEntries
            .filterSmart {
                it.schemaPsi.any { schemaPsi ->
                    PsiManager.getInstance(psiElement.project).areElementsEquivalent(schemaPsi, psiElement)
                }
            }
            .toList()
    }

    override fun allMethodSchemaMappingEntries(): List<QLMethodSchemaMappingIndexEntry> {
        return methodSchemaMappingEntries.toList()
    }

    override fun allMethodBatchMappingEntries(): List<QLMethodBatchMappingIndexEntry> {
        return methodBatchMappingEntries.toList()
    }

    override fun schemaMappingByMethod(uMethod: UMethod): List<SchemaMappingIndexEntry> {
        val sourcePsi = uMethod.sourcePsi
        if (sourcePsi == null) {
            return emptyList()
        }

        val psiManager = PsiManager.getInstance(sourcePsi.project)
        val schemaMethodMappings = methodSchemaMappingEntries
            .filterSmart { psiManager.areElementsEquivalent(it.methodPsi, sourcePsi) }
        val batchMethodMappings = methodBatchMappingEntries
            .filterSmart { psiManager.areElementsEquivalent(it.methodPsi, sourcePsi) }
        return (schemaMethodMappings union batchMethodMappings).toList()
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
