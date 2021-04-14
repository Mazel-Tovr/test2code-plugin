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
import com.epam.drill.plugins.test2code.jvm.*
import com.epam.drill.plugins.test2code.util.*
import com.epam.kodux.util.*
import kotlinx.atomicfu.*
import kotlinx.collections.immutable.*
import kotlinx.serialization.*
import java.util.concurrent.*

@Serializable
sealed class Session : Iterable<ExecClassData>, java.io.Serializable {
    abstract val id: String
    abstract val testType: String
    abstract val tests: Set<TypedTest>
    abstract val testStats: Map<TypedTest, TestStats>

    //    @Transient
    var calculated: MutableMap<ExecClassData, ClassCounter> = mutableMapOf()
}

class ActiveSession(
    override val id: String,
    override val testType: String,
    val isGlobal: Boolean = false,
    val isRealtime: Boolean = false,
) : Session() {
    @Transient
    var classMapping: Map<String, ClassCoverage> = emptyMap()

    override val tests: Set<TypedTest>
        get() = _probes.value.keys

    override val testStats: Map<TypedTest, TestStats>
        get() = _testRun.value?.tests?.associate {
            TypedTest(type = testType, name = it.name) to TestStats(
                duration = it.finishedAt - it.startedAt,
                result = it.result
            )
        } ?: emptyMap()

    private val _probes = atomic(
        persistentMapOf<TypedTest, PersistentMap<Long, ExecClassData>>()
    )

    private val _testRun = atomic<TestRun?>(null)

    fun addAll(dataPart: Collection<ExecClassData>) =
        dataPart.map { probe ->
            probe.id?.let { probe } ?: probe.copy(id = probe.id())
        }.forEach { probe ->
            if (true in probe.probes) {
                val typedTest = probe.testName.weakIntern().typedTest(testType.weakIntern())
                _probes.update { map ->
                    (map[typedTest] ?: persistentHashMapOf()).let { testData ->
                        val probeId = probe.id()
                        if (probeId in testData) {
                            testData.getValue(probeId).run {
                                val merged = probes.merge(probe.probes)
                                merged.takeIf { it != probes }?.let {
                                    copy(probes = merged).let { data ->
                                        classMapping[data.className]?.toCoverageUnit(data.probes)?.let {
                                            calculated[data] = it
                                        }
                                        testData.put(probeId, data)
                                    }
                                }
                            }
                        } else {
                            probe.copy(testName = typedTest.name).let { data ->
                                classMapping[data.className]?.toCoverageUnit(data.probes)?.let {
                                    calculated[data] = it
                                }
                                testData.put(probeId, data)
                            }
                        }
                    }?.let { map.put(typedTest, it) } ?: map
                }
            }
        }

    fun setTestRun(testRun: TestRun) {
        _testRun.update { current ->
            current?.let { it + testRun } ?: testRun
        }
    }

    override fun iterator(): Iterator<ExecClassData> = _probes.value.values.flatMap { it.values }.iterator()

    fun finish() = _probes.value.run {

        println(_probes.value.size)
        FinishedSession(
            id = id,
            testType = testType,
            tests = HashSet(_testRun.value?.tests?.takeIf { it.any() }?.let { tests ->
                keys + tests.map { it.name.typedTest(testType) }
            } ?: keys),
            testStats = _testRun.value?.tests?.associate {
                TypedTest(type = testType, name = it.name) to TestStats(
                    duration = it.finishedAt - it.startedAt,
                    result = it.result
                )
            } ?: emptyMap(),
            probes = values.flatMap { it.values },
        ).apply {
            calculated = this@ActiveSession.calculated
        }
    }
}

@Serializable
data class FinishedSession(
    override val id: String,
    override val testType: String,
    override val tests: Set<TypedTest>,
    override val testStats: Map<TypedTest, TestStats> = emptyMap(),
    val probes: List<ExecClassData>,
) : Session(), java.io.Serializable {
    override fun iterator(): Iterator<ExecClassData> = probes.iterator()

    override fun equals(other: Any?): Boolean = other is FinishedSession && id == other.id

    override fun hashCode(): Int = id.hashCode()
}

private operator fun TestRun.plus(other: TestRun) = copy(
    startedAt = startedAt.takeIf { it < other.startedAt } ?: other.startedAt,
    finishedAt = finishedAt.takeIf { it > other.finishedAt } ?: other.finishedAt,
    tests = tests + other.tests
)
