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
import com.epam.drill.plugins.test2code.coverage.*
import com.epam.drill.plugins.test2code.util.*
import java.util.stream.*

//TODO Rewrite all of this, remove the file

internal data class BuildMethods(
    val totalMethods: MethodsInfo = MethodsInfo(),
    val newMethods: MethodsInfo = MethodsInfo(),
    val modifiedNameMethods: MethodsInfo = MethodsInfo(),
    val modifiedDescMethods: MethodsInfo = MethodsInfo(),
    val modifiedBodyMethods: MethodsInfo = MethodsInfo(),
    val allModifiedMethods: MethodsInfo = MethodsInfo(),
    val unaffectedMethods: MethodsInfo = MethodsInfo(),
    val deletedMethods: MethodsInfo = MethodsInfo(),
)

internal data class CoverageInfoSet(
    val associatedTests: List<AssociatedTests>,
    val coverage: Coverage,
    val buildMethods: BuildMethods = BuildMethods(),
    val packageCoverage: List<JavaPackageCoverage> = emptyList(),
    val tests: List<TestCoverageDto> = emptyList(),
    val coverageByTests: CoverageByTests,
)

fun Map<CoverageKey, List<TypedTest>>.getAssociatedTests(): List<AssociatedTests> = entries.parallelStream().map { (key, tests) ->
        AssociatedTests(
            id = key.id,
            packageName = key.packageName,
            className = key.className,
            methodName = key.className.methodName(key.methodName),
            tests = tests.stream().sorted { o1, o2 -> o1.name.compareTo(o2.name) }.collect(Collectors.toList())
        )
    }.sorted { o1, o2 -> o1.methodName.compareTo(o2.methodName) }.collect(Collectors.toList())

internal suspend fun CoverContext.calculateBundleMethods(
    bundleCoverage: BundleCounter,
    onlyCovered: Boolean = false
): BuildMethods {
    val associate = methods.associate { it.key to it.toCovered() }
    val pair = associate to methodChanges
    return pair.calculateBundleMethods(bundleCoverage, onlyCovered)
}

private fun Iterable<Method>.toInfo(
    covered: Map<String, CoverMethod>,
) = MethodsInfo(
    totalCount = count(),
    coveredCount = count { covered[it.key]?.count?.covered ?: 0 > 0 },
    methods = mapNotNull { covered[it.key] }
)

internal suspend fun Pair<Map<String, CoverMethod>, DiffMethods>.calculateBundleMethods(
    bundleCoverage: BundleCounter,
    onlyCovered: Boolean = false
): BuildMethods = let { (methods, diffMethods) ->
    methods.toCoverMap(bundleCoverage, onlyCovered).let { covered: Map<String, CoverMethod> ->
        diffMethods.run {
            val values = covered.values
            BuildMethods(
                totalMethods = MethodsInfo(
                    totalCount = values.count(),
                    coveredCount = values.count { it.count.covered > 0 },
                    methods = values.toList()
                ),
                newMethods = new.toInfo(covered),
                allModifiedMethods = modified.toInfo(covered),
                unaffectedMethods = unaffected.toInfo(covered),
                deletedMethods = MethodsInfo(
                    totalCount = deleted.count(),
                    coveredCount = deletedWithCoverage.count(),
                    methods = deleted.map { it.toCovered(deletedWithCoverage[it]) }
                )
            )
        }
    }
}

//todo what is it??
//internal fun Map<TypedTest, BundleCounter>.methodsCoveredByTest(
//    context: CoverContext,
//    cache: AtomicCache<TypedTest, MethodsCoveredByTest>?,
//    finalizedTests: Iterable<TypedTest>
//): List<MethodsCoveredByTest> {
//    val pair = Pair(context.methods.associate { it.key to it.toCovered() }, context.methodChanges)
//    return runBlocking(allAvailableProcessDispatcher) {
//        val subCollectionSize = (size / availableProcessors).takeIf { it > 0 } ?: 1
//        asIterable().chunked(subCollectionSize).map {
//            async {
//                it.map { (typedTest, bundle) ->
//                    cache?.get(typedTest) ?: pair.calculateBundleMethods(bundle, true).let { changes ->
//                        MethodsCoveredByTest(
//                            id = typedTest.id(),
//                            testName = typedTest.name,
//                            testType = typedTest.type,
//                            allMethods = changes.totalMethods.methods,
//                            newMethods = changes.newMethods.methods,
//                            modifiedMethods = changes.allModifiedMethods.methods,
//                            unaffectedMethods = changes.unaffectedMethods.methods
//                        ).also {
//                            if (typedTest in finalizedTests)
//                                cache?.set(typedTest, it)
//                        }
//                    }
//                }
//
//            }
//        }.flatMap {
//            it.await()
//        }
//
//    }
//}
//

