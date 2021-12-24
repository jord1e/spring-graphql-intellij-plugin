/*
 * Copyright (C) 2021 Jordie
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package nl.jrdie.idea.springql.services

import nl.jrdie.idea.springql.index.QLIdeIndex
import nl.jrdie.idea.springql.index.QLIdeIndexBuildingProcessor
import nl.jrdie.idea.springql.index.entry.QLMethodBatchMappingIndexEntry
import nl.jrdie.idea.springql.index.entry.QLMethodSchemaMappingIndexEntry
import nl.jrdie.idea.springql.svc.QLIdeService
import nl.jrdie.idea.springql.utils.QLIdeUtil
import org.jetbrains.uast.UAnnotation
import org.jetbrains.uast.UMethod
import org.jetbrains.uast.getParentOfType

class QLAnnotationIndexProcessor<B : QLIdeIndex.Builder<B>>(
    private val ideService: QLIdeService
) : QLIdeIndexBuildingProcessor<UAnnotation, B> {

    override fun process(t: UAnnotation, indexBuilder: B): B {
        val src = t.sourcePsi
        if (src == null || !ideService.isApplicableProject(src.project)) {
            return indexBuilder
        }

        when {
            ideService.isSchemaMappingAnnotation(t) -> {
                val uMethod = t.getParentOfType<UMethod>()
                if (uMethod != null) {
                    return processMethodSchemaMapping(indexBuilder, uMethod, t)
                }
            }
            ideService.isBatchMappingAnnotation(t) -> {
                val uMethod = t.getParentOfType<UMethod>()
                if (uMethod != null) {
                    return processMethodBatchMapping(indexBuilder, uMethod, t)
                }
            }
        }

        return indexBuilder
    }

    private fun processMethodSchemaMapping(indexBuilder: B, uMethod: UMethod, uAnnotation: UAnnotation): B {
        val typeName = QLIdeUtil.getSchemaMappingTypeName(uMethod)
        checkNotNull(typeName)
        val field = QLIdeUtil.getGraphQlField(uMethod)
        checkNotNull(field)


//        val schemaPsi = ideService.schemaRegistry.getSchemaPsiForObject(typeName, field)
        val schemaPsi = ideService.schemaRegistry.getAllSchemaPsiForObject(typeName, field)

        println("Added Method SchemaMapping ${typeName}.${field} -> $schemaPsi")

        return indexBuilder.withMethodSchemaMapping(
            QLMethodSchemaMappingIndexEntry(
                typeName,
                field,
                uAnnotation.sourcePsi!!,
                uMethod.sourcePsi!!,
                schemaPsi
            )
        )
    }

    private fun processMethodBatchMapping(indexBuilder: B, uMethod: UMethod, uAnnotation: UAnnotation): B {
        val typeName = QLIdeUtil.getSchemaMappingTypeName(uMethod)
        checkNotNull(typeName)
        val field = QLIdeUtil.getGraphQlField(uMethod)
        checkNotNull(field)


//        val schemaPsi = ideService.schemaRegistry.getSchemaPsiForObject(typeName, field)
        val schemaPsi = ideService.schemaRegistry.getAllSchemaPsiForObject(typeName, field)

        println("Added Method BatchMapping ${typeName}.${field} -> $schemaPsi")

        return indexBuilder.withMethodBatchMapping(
            QLMethodBatchMappingIndexEntry(
                typeName,
                field,
                uAnnotation.sourcePsi!!,
                uMethod.sourcePsi!!,
                ideService.isValidBatchMappingReturnType(uMethod),
                schemaPsi
            )
        )
    }

}