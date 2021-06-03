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

import com.epam.drill.plugin.api.processing.*
import com.epam.drill.plugins.test2code.common.api.*
import kotlinx.atomicfu.*
import kotlinx.collections.immutable.*
import kotlinx.coroutines.*
import kotlin.coroutines.*

/**
 * Provides boolean array for the probe.
 * Implementations must be kotlin singleton objects.
 */
typealias ProbeArrayProvider = (ClassId, ClassName, Int, Int, Int) -> Probes

typealias RealtimeHandler = (Sequence<ExecDatum>) -> Unit

interface SessionProbeArrayProvider : ProbeArrayProvider {

    fun start(
        sessionId: String,
        isGlobal: Boolean,
        testName: String? = null,
        realtimeHandler: RealtimeHandler = {},
    )

    fun stop(sessionId: String): Sequence<ExecDatum>?
    fun stopAll(): List<Pair<String, Sequence<ExecDatum>>>
    fun cancel(sessionId: String)
    fun cancelAll(): List<String>
}

const val DRIlL_TEST_NAME = "drill-test-name"

class ExecDatum(
    val id: Long,
    val name: String,
    val probes: Probes,
    val testName: String = "",
)

////TODO STUB
//fun Iterable<MethodExecDatum>.merge(): Probes = Probes() //STUB
//
////TODO STUB
//fun ClassId.merge(methodExecData: MethodExecData): ExecDatum = methodExecData.values.let {
//    it.first().run {
//        ExecDatum(id, name, it.merge(), testName)
//    }
//}
//
////TODO class/string pool might improve memory usage
//class MethodExecDatum(
//    val id: Long,
//    val name: String,
//    val probes: Probes,
//    val testName: String = "",
//)


fun ExecDatum.toExecClassData() = ExecClassData(
    id = id,
    className = name,
    probes = probes,
    testName = testName
)


internal object ProbeWorker : CoroutineScope {
    override val coroutineContext: CoroutineContext = run {
        java.util.concurrent.Executors.newFixedThreadPool(4).asCoroutineDispatcher() + SupervisorJob()
    }
}
typealias RangeId = Long
typealias Stub = PersistentMap<RangeId, Boolean>
typealias ClassExecData = PersistentMap<ClassId, Pair<Stub, ExecDatum>>
typealias ClassName = String
typealias TestName = String
typealias ClassId = Long
typealias MethodId = String

private val probesStub = ProbesStub()

/**
 * A container for session runtime data and optionally runtime data of tests
 * TODO ad hoc implementation, rewrite to something more descent
 */
class ExecRuntime(
    realtimeHandler: RealtimeHandler,
) : (ClassId, ClassName, Int, String, Int, Int) -> Probes {

    fun collect(): Sequence<ExecDatum> = _execData.getAndUpdate { it.clear() }.values.asSequence().run {
        flatMap { map -> map.values.map { it.second }.asSequence() }
    }

    private val _execData = atomic(persistentHashMapOf<TestName, ClassExecData>())

    private val job = ProbeWorker.launch {
        while (true) {
            delay(2000L)
            realtimeHandler(collect())
        }
    }


    override fun invoke(
        id: ClassId,
        name: ClassName,
        probeCount: Int,
        testName: TestName,
        start: Int,
        end: Int,
    ): Probes {
        val key = key(start, end)
        return _execData.updateAndGet { tests ->
            (tests[testName] ?: persistentHashMapOf()).let { execData ->
                if (id !in execData) {
                    //init
                    val stub = persistentHashMapOf(key to false)
                    val mutatedData = execData.put(
                        id, stub to ExecDatum(
                            id = id,
                            name = name,
                            probes = Probes(probeCount),
                            testName = testName
                        )
                    )
                    tests.put(testName, mutatedData)
                } else {
                    val stubToEecDatum = execData[id]!!
                    val probes = stubToEecDatum.second.probes
                    val first = stubToEecDatum.first
                    if (first[key] == true) {
                        return@updateAndGet tests
                    }
                    for (i in start until end) {
                        if (!probes.get(i))
                            return@updateAndGet tests.put(testName,
                                execData.put(id, stubToEecDatum.copy(first = first.put(key, false))))
                    }
                    tests.put(testName, execData.put(id, stubToEecDatum.copy(first = first.put(key, true))))
                }
            }
        }.getValue(testName).getValue(id).let {
            if (it.first[key] == true) {
                probesStub
            } else {
                it.second.probes
            }
        }
    }

    fun close() {
        job.cancel()
    }

    private fun key(covered: Int, total: Int): RangeId = (covered.toLong() shl 32) or (total.toLong() and 0xFFFFFFFFL)
}

/**
 * Simple probe array provider that employs a lock-free map for runtime data storage.
 * This class is intended to be an ancestor for a concrete probe array provider object.
 * The provider must be a Kotlin singleton object, otherwise the instrumented probe calls will fail.
 */
open class SimpleSessionProbeArrayProvider(
    defaultContext: AgentContext? = null,
) : SessionProbeArrayProvider {

    var defaultContext: AgentContext?
        get() = _defaultContext.value
        set(value) {
            _defaultContext.value = value
        }

    private val runtimes get() = _runtimes.value

    private val _defaultContext = atomic(defaultContext)

    private val _context = atomic<AgentContext?>(null)

    private val _globalContext = atomic<AgentContext?>(null)

    private val _runtimes = atomic(persistentHashMapOf<String, ExecRuntime>())

    private val _stubProbes = atomic(ProbesStub())

    override fun invoke(
        id: Long,
        name: String,
        probeCount: Int,
        start: Int,
        end: Int,
    ): Probes = _context.value?.let {
        it(id, name, probeCount, start, end)
    } ?: _globalContext.value?.let {
        it(id, name, probeCount, start, end)
    } ?: _stubProbes.value

    private operator fun AgentContext.invoke(
        id: Long,
        name: String,
        probeCount: Int,
        start: Int,
        end: Int,
    ): Probes? = run {
        val sessionId = this()
        runtimes[sessionId]?.let { sessionRuntime: ExecRuntime ->
            val testName = this[DRIlL_TEST_NAME] ?: "unspecified"
            sessionRuntime(id, name, probeCount, testName, start, end)
        }
    }

    override fun start(
        sessionId: String,
        isGlobal: Boolean,
        testName: String?,
        realtimeHandler: RealtimeHandler,
    ) {
        if (isGlobal) {
            _globalContext.value = GlobalContext(sessionId, testName)
            add(sessionId, realtimeHandler)
        } else {
            _context.update { it ?: defaultContext }
            add(sessionId, realtimeHandler)
        }
    }

    override fun stop(sessionId: String): Sequence<ExecDatum>? = remove(sessionId)?.collect()

    override fun stopAll(): List<Pair<String, Sequence<ExecDatum>>> = _runtimes.getAndUpdate {
        _context.value = null
        _globalContext.value = null
        it.clear()
    }.map { (id, runtime) ->
        runtime.close()
        id to runtime.collect()
    }

    override fun cancel(sessionId: String) {
        remove(sessionId)
    }

    override fun cancelAll(): List<String> = _runtimes.getAndUpdate {
        _context.value = null
        _globalContext.value = null
        it.clear()
    }.map { (id, runtime) ->
        runtime.close()
        id
    }

    private fun add(sessionId: String, realtimeHandler: RealtimeHandler) {
        _runtimes.update {
            if (sessionId !in it) {
                it.put(sessionId, ExecRuntime(realtimeHandler))
            } else it
        }
    }

    private fun remove(sessionId: String): ExecRuntime? = (_runtimes.getAndUpdate { runtimes ->
        (runtimes - sessionId).also { map ->
            if (map.none()) {
                _context.value = null
                _globalContext.value = null
            } else {
                val globalSessionId = _globalContext.value?.invoke()
                if (map.size == 1 && globalSessionId in map) {
                    _context.value = null
                }
                if (globalSessionId == sessionId) {
                    _globalContext.value = null
                }
            }
        }
    }[sessionId])?.also(ExecRuntime::close)

}

private class GlobalContext(
    private val sessionId: String,
    private val testName: String?,
) : AgentContext {
    override fun get(key: String): String? = testName?.takeIf { key == DRIlL_TEST_NAME }

    override fun invoke(): String? = sessionId
}
