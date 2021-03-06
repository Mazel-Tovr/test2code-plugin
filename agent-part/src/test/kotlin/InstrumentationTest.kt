package com.epam.drill.plugins.test2code

import com.epam.drill.logger.api.*
import kotlinx.atomicfu.*
import kotlinx.collections.immutable.*
import org.jacoco.core.analysis.*
import org.jacoco.core.data.*
import org.jacoco.core.internal.data.*
import kotlin.test.*


class InstrumentationTest {

    companion object {
        const val sessionId = "xxx"

        val instrContextStub: com.epam.drill.plugin.api.processing.IDrillContex =
            object : com.epam.drill.plugin.api.processing.IDrillContex {
                override fun get(key: String): String? = when (key) {
                    DRIlL_TEST_NAME -> "test"
                    else -> null
                }

                override fun invoke(): String? = sessionId

            }
    }


    object TestProbeArrayProvider : SimpleSessionProbeArrayProvider(instrContextStub)

    val instrument = instrumenter(TestProbeArrayProvider, "".namedLogger(appender = NopLogAppender))

    val memoryClassLoader = MemoryClassLoader()

    val targetClass = TestTarget::class.java

    val originalBytes = targetClass.readBytes()

    val originalClassId = CRC64.classId(originalBytes)

    private val _runtimeData = atomic(persistentListOf<ExecDatum>())

    @Test
    fun `instrumented class should be larger the the original`() {
        val instrumented = instrument(targetClass.name, originalClassId, originalBytes)!!
        assertTrue { instrumented.count() > originalBytes.count() }
    }

    @Test
    fun `should provide coverage for run with the instrumented class`() {
        addInstrumentedClass()
        val instrumentedClass = memoryClassLoader.loadClass(targetClass.name)
        TestProbeArrayProvider.start(sessionId, false) { dataSeq ->
            _runtimeData.update { it + dataSeq }

        }
        @Suppress("DEPRECATION") val runnable = instrumentedClass.newInstance() as Runnable
        runnable.run()
        val runtimeData = _runtimeData.updateAndGet {
            it + (TestProbeArrayProvider.stop(sessionId) ?: emptySequence())
        }
        val executionData = ExecutionDataStore()
        runtimeData.forEach { executionData.put(ExecutionData(it.id, it.name, it.probes)) }
        val coverageBuilder = CoverageBuilder()
        val analyzer = Analyzer(executionData, coverageBuilder)
        analyzer.analyzeClass(originalBytes, targetClass.name)
        val coverage = coverageBuilder.getBundle("all")
        val counter = coverage.instructionCounter
        assertEquals(27, counter.coveredCount)
        assertEquals(2, counter.missedCount)
    }

    @Test
    fun `should associate execution data with test name and type gathered from request headers`() {
        addInstrumentedClass()
        val instrumentedClass = memoryClassLoader.loadClass(targetClass.name)
        TestProbeArrayProvider.start(sessionId, false)
        @Suppress("DEPRECATION") val runnable = instrumentedClass.newInstance() as Runnable
        runnable.run()
        val runtimeData = TestProbeArrayProvider.stop(sessionId)!!.toList()
        val assestions = runtimeData.map { execDatum ->
            { assertEquals("test", execDatum.testName) }
        }.toTypedArray()
        org.junit.jupiter.api.assertAll(*assestions)
    }

    private fun addInstrumentedClass() {
        val name = targetClass.name
        val instrumented = instrument(name, originalClassId, originalBytes)!!
        memoryClassLoader.addDefinition(name, instrumented)
    }
}


class MemoryClassLoader : ClassLoader() {
    private val definitions = mutableMapOf<String, ByteArray?>()

    fun addDefinition(name: String, bytes: ByteArray) {
        definitions[name] = bytes
    }

    override fun loadClass(name: String?, resolve: Boolean): Class<*> {
        val bytes = definitions[name]
        return if (bytes != null) {
            defineClass(name, bytes, 0, bytes.size)
        } else {
            super.loadClass(name, resolve)
        }
    }
}
