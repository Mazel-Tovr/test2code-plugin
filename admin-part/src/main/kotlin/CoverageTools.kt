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

//TODO Rewrite all of this, remove the file

internal data class BuildMethods(
    val totalMethods: MethodsInfo = MethodsInfo(),
    val newMethods: MethodsInfo = MethodsInfo(),
    val modifiedNameMethods: MethodsInfo = MethodsInfo(),
    val modifiedDescMethods: MethodsInfo = MethodsInfo(),
    val modifiedBodyMethods: MethodsInfo = MethodsInfo(),
    val allModifiedMethods: MethodsInfo = MethodsInfo(),
    val unaffectedMethods: MethodsInfo = MethodsInfo(),
    val deletedMethods: MethodsInfo = MethodsInfo()
)

internal data class CoverageInfoSet(
    val associatedTests: List<AssociatedTests>,
    val coverage: Coverage,
    val buildMethods: BuildMethods = BuildMethods(),
    val packageCoverage: List<JavaPackageCoverage> = emptyList(),
    val tests: List<TestCoverageDto> = emptyList(),
    val coverageByTests: CoverageByTests,
    val methodsCoveredByTest: List<MethodsCoveredByTest> = emptyList()
)

fun Map<CoverageKey, List<TypedTest>>.getAssociatedTests() = map { (key, tests) ->
    AssociatedTests(
        id = key.id.intern(),
        packageName = key.packageName.intern(),
        className = key.className.intern(),
        methodName = key.className.methodName(key.methodName).intern(),
        tests = tests.sortedBy { it.name.intern() }
    )
}.sortedBy { it.methodName }

internal fun CoverContext.calculateBundleMethods(
    bundleCoverage: BundleCounter,
    onlyCovered: Boolean = false
): BuildMethods = methods.toCoverMap(bundleCoverage, onlyCovered).let { covered ->
    methodChanges.run {
        BuildMethods(
            totalMethods = covered.keys.toInfo(covered),
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

internal fun Map<TypedTest, BundleCounter>.methodsCoveredByTest(
    context: CoverContext,
    cache: AtomicCache<BundleCounter, MethodsCoveredByTest>?
): List<MethodsCoveredByTest> = map { (typedTest, bundle) ->
    cache?.get(bundle) ?: context.calculateBundleMethods(bundle, true).let { changes ->
        MethodsCoveredByTest(
            id = typedTest.id(),
            testName = typedTest.name,
            testType = typedTest.type,
            allMethods = changes.totalMethods.methods,
            newMethods = changes.newMethods.methods,
            modifiedMethods = changes.allModifiedMethods.methods,
            unaffectedMethods = changes.unaffectedMethods.methods
        ).also { cache?.set(bundle, it) }
    }
}

private fun Iterable<Method>.toInfo(
    covered: Map<Method, CoverMethod>
) = MethodsInfo(
    totalCount = count(),
    coveredCount = count { covered[it]?.count?.covered ?: 0 > 0 },
    methods = mapNotNull(covered::get)
)
