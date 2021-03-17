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
package com.epam.drill.plugins.test2code.util

import com.epam.drill.plugins.test2code.*
import com.epam.drill.plugins.test2code.api.*
import com.epam.drill.plugins.test2code.coverage.*
import kotlinx.coroutines.*
import org.jacoco.core.internal.data.*
import java.net.*
import java.util.*
import java.util.concurrent.*

fun currentTimeMillis() = System.currentTimeMillis()

fun genUuid() = "${UUID.randomUUID()}"

internal val availableProcessors = Runtime.getRuntime().availableProcessors()

internal val allAvailableProcessDispatcher = Executors.newFixedThreadPool(availableProcessors).asCoroutineDispatcher()

tailrec fun Int.gcd(other: Int): Int = takeIf { other == 0 } ?: other.gcd(rem(other))

fun String.methodName(name: String): String = when (name) {
    "<init>" -> toShortClassName()
    "<clinit>" -> "static ${toShortClassName()}"
    else -> name
}

internal fun String.urlDecode(): String = takeIf { '%' in it }?.run {
    runCatching { URLDecoder.decode(this, "UTF-8") }.getOrNull()
} ?: this

internal fun String.toShortClassName(): String = substringAfterLast('/').intern()

val String.crc64: String get() = CRC64.classId(toByteArray()).toString(Character.MAX_RADIX).intern()

internal fun String.crc64(): Long = CRC64.classId(toByteArray())

infix fun Number.percentOf(other: Number): Double = when (val dOther = other.toDouble()) {
    0.0 -> 0.0
    else -> toDouble() * 100.0 / dOther
}


fun BundleCounter.copyIntern() = copy(
    name = name.intern(),
    packages = packages.map { packageCounter ->
        packageCounter.copy(
            name = packageCounter.name.intern(),
            classes = packageCounter.classes.map { classCounter ->
                classCounter.copy(
                    name = classCounter.name.intern(),
                    path = classCounter.path.intern(),
                    methods = classCounter.methods.map { methodCounter ->
                        methodCounter.copy(
                            name = methodCounter.name.intern(),
                            desc = methodCounter.desc.intern(),
                            decl = methodCounter.decl.intern()
                        )
                    }
                )
            }
        )
    }
)

fun TypedTest.copyIntern() = copy(name = name.intern(), type = type.intern())

fun BundleCounters.copyIntern() = copy(
    all = all.copyIntern(),
    testTypeOverlap = testTypeOverlap.copyIntern(),
    overlap = overlap.copyIntern(),
    byTestType = byTestType.asSequence().associate { entry ->
        entry.key.intern() to entry.value.copyIntern()
    },
    byTest = byTest.asSequence().associate { entry ->
        entry.key.copyIntern() to entry.value.copyIntern()
    },
    statsByTest = statsByTest.mapKeys { it.key.copyIntern() }
)

fun BuildTests.copyIntern() = copy(
    tests = tests.asSequence().associate { entry ->
        entry.key.intern() to entry.value.map { it.intern() }
    },
    assocTests = assocTests.map { it.copyIntern() }.toSet()
)

fun AssociatedTests.copyIntern() = copy(
    id = id.intern(),
    packageName = packageName.intern(),
    className = className.intern(),
    methodName = methodName.intern(),
    tests = tests.map { it.copyIntern() }
)
