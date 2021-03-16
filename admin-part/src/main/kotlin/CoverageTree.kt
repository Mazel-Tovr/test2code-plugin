/**
 * Copyright 2020 EPAM Systems
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.epam.drill.plugins.test2code

import com.epam.drill.plugins.test2code.api.*
import com.epam.drill.plugins.test2code.common.api.*
import com.epam.drill.plugins.test2code.coverage.*
import com.epam.drill.plugins.test2code.util.*

internal fun Iterable<AstEntity>.toPackages(): List<JavaPackageCoverage> = run {
    groupBy(AstEntity::path).entries.map { (path, astEntities) ->
        JavaPackageCoverage(
            id = path.crc64,
            name = path,
            totalClassesCount = astEntities.count(),
            totalMethodsCount = astEntities.flatMap(AstEntity::methodsWithProbes).count(),
            totalCount = astEntities.flatMap(AstEntity::methodsWithProbes).map(AstMethod::count).sum(),
            classes = astEntities.mapNotNull { ast ->
                ast.methodsWithProbes().takeIf { it.any() }?.let { methods ->
                    JavaClassCoverage(
                        id = "$path.${ast.name}".crc64.intern(),
                        name = ast.name.intern(),
                        path = path.intern(),
                        totalMethodsCount = methods.count(),
                        totalCount = methods.sumBy { it.count },
                        methods = methods.fold(listOf()) { acc, astMethod ->
                            val desc = astMethod.toDesc()
                            acc + JavaMethodCoverage(
                                id = "$path.${ast.name}.${astMethod.name}".crc64.intern(),
                                name = astMethod.name.intern(),
                                desc = desc.intern(),
                                count = astMethod.probes.size,
                                decl = desc.intern(),
                                probeRange = (acc.lastOrNull()?.probeRange?.last?.inc() ?: 0).let {
                                    ProbeRange(it, it + astMethod.probes.lastIndex)
                                }
                            )
                        },
                        probes = methods.flatMap(AstMethod::probes)
                    )
                }
            }
        )
    }
}

internal fun Iterable<PackageCounter>.toPackages(
    parsedClasses: Map<String, List<Method>>
): List<JavaPackageCoverage> = mapNotNull { packageCoverage ->
    packageCoverage.classes.classTree(parsedClasses).takeIf { it.any() }?.let { classes ->
        JavaPackageCoverage(
            id = packageCoverage.coverageKey().id,
            name = packageCoverage.name,
            totalClassesCount = classes.count(),
            totalMethodsCount = classes.sumBy { it.totalMethodsCount },
            totalCount = packageCoverage.count.total,
            classes = classes
        )
    }
}.toList()


internal fun Iterable<JavaPackageCoverage>.treeCoverage(
    bundle: BundleCounter,
    assocTestsMap: Map<CoverageKey, List<TypedTest>>
): List<JavaPackageCoverage> = run {
    val bundleMap = bundle.packages.associateBy { it.coverageKey().id }
    map { pkg ->
        bundleMap[pkg.id]?.run {
            pkg.copy(
                coverage = count.copy(total = pkg.totalCount).percentage(),
                coveredClassesCount = classCount.covered,
                coveredMethodsCount = methodCount.covered,
                assocTestsCount = assocTestsMap[coverageKey()]?.count() ?: 0,
                classes = pkg.classes.classCoverage(classes, assocTestsMap)
            )
        } ?: pkg
    }
}

private fun Collection<ClassCounter>.classTree(
    parsedClasses: Map<String, List<Method>>
): List<JavaClassCoverage> = mapNotNull { classCoverage ->
    parsedClasses[classCoverage.fullName]?.let { parsedMethods ->
        val classKey = classCoverage.coverageKey()
        val methods = classCoverage.toMethodCoverage { methodCov ->
            parsedMethods.any { it.name == methodCov.name && it.desc == methodCov.desc }
        }
        JavaClassCoverage(
            id = classKey.id.intern(),
            name = classCoverage.name.toShortClassName(),
            path = classCoverage.name.intern(),
            totalMethodsCount = methods.count(),
            totalCount = methods.sumBy { it.count },
            methods = methods,
            probes = emptyList()
        )
    }
}.toList()

private fun List<JavaClassCoverage>.classCoverage(
    classCoverages: Collection<ClassCounter>,
    assocTestsMap: Map<CoverageKey, List<TypedTest>>
): List<JavaClassCoverage> = run {
    val bundleMap = classCoverages.associateBy { it.coverageKey().id }
    map { classCov ->
        bundleMap[classCov.id]?.run {
            classCov.copy(
                coverage = count.percentage(),
                coveredMethodsCount = methods.count { it.count.covered > 0 },
                assocTestsCount = assocTestsMap[coverageKey()]?.count() ?: 0,
                methods = toMethodCoverage(assocTestsMap)
            )
        } ?: classCov
    }
}

internal fun ClassCounter.toMethodCoverage(
    assocTestsMap: Map<CoverageKey, List<TypedTest>> = emptyMap(),
    filter: (MethodCounter) -> Boolean = { true }
): List<JavaMethodCoverage> {
    return methods.filter(filter).map { methodCoverage ->
        val methodKey = methodCoverage.coverageKey(this)
        JavaMethodCoverage(
            id = methodKey.id.intern(),
            name = name.methodName(methodCoverage.name),
            desc = methodCoverage.desc.intern(),
            decl = methodCoverage.decl.intern(),
            coverage = methodCoverage.count.percentage(),
            count = methodCoverage.count.total,
            assocTestsCount = assocTestsMap[methodKey]?.count() ?: 0
        )
    }.toList()
}

internal fun AstMethod.toDesc(): String = params.joinToString(
    prefix = "(", postfix = "):$returnType"
).intern()

private fun AstEntity.methodsWithProbes(): List<AstMethod> = methods.filter { it.probes.any() }
