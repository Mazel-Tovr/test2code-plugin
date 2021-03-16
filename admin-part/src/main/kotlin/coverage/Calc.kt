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
package com.epam.drill.plugins.test2code.coverage

import com.epam.drill.plugins.test2code.api.*
import com.epam.drill.plugins.test2code.common.api.*
import com.epam.drill.plugins.test2code.util.*

internal fun Sequence<ExecClassData>.bundle(
    tree: PackageTree
): BundleCounter = run {
    val probeCounts: Map<String, Int> = tree.packages.run {
        flatMap { it.classes }.associateBy({ "${it.path}/${it.name}" }) { it.totalCount }
    }
    val probesByClasses: Map<String, List<Boolean>> = filter {
        it.className in probeCounts
    }.groupBy(ExecClassData::className).mapValues { (className, execDataList) ->
        val initialProbe = BooleanArray(probeCounts.getValue(className)) { false }.toList()
        execDataList.map(ExecClassData::probes).fold(initialProbe) { acc, probes ->
            acc.merge(probes)
        }
    }
    val classMethods = tree.packages.flatMap { it.classes }.associate {
        "${it.path}/${it.name}" to it.methods
    }
    val covered = probesByClasses.values.sumBy { probes -> probes.count { it } }
    val packages = probesByClasses.keys.groupBy {
        it.substringBeforeLast("/")
    }.map { (pkgName, classNames) ->
        val classes = classNames.map { className ->
            val probes = probesByClasses.getValue(className)
            ClassCounter(
                path = pkgName.intern(),
                name = className.toShortClassName().intern(),
                count = probes.toCount(),
                methods = classMethods.getValue(className).map {
                    val methodProbes = probes.slice(it.probeRange)
                    MethodCounter(it.name.intern(), it.desc.intern(), it.decl.intern(), methodProbes.toCount())
                }
            )
        }
        PackageCounter(
            name = pkgName.intern(),
            count = classNames.flatMap { probesByClasses[it] ?: emptyList() }.toCount(),
            classCount = Count(
                classNames.count { name -> probesByClasses.getValue(name).any { it } },
                classNames.size
            ),
            methodCount = Count(
                classes.sumBy { c ->  c.methods.count { it.count.covered > 0 } },
                classes.sumBy { it.methods.count() }
            ),
            classes = classes
        )
    }
    BundleCounter(
        name = "",
        count = Count(covered, tree.totalCount),
        methodCount = packages.run {
            Count(sumBy { it.methodCount.covered }, sumBy { it.methodCount.total })
        },
        classCount = packages.run {
            Count(sumBy { it.classCount.covered }, sumBy { it.classCount.total })
        },
        packageCount = packages.run {
            Count(count { it.classCount.covered > 0 }, count())
        },
        packages = packages
    )
}
