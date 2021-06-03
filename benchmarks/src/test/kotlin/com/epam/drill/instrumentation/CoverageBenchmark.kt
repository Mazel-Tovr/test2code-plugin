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
package com.epam.drill.instrumentation

import com.epam.drill.instrumentation.data.*
import com.epam.drill.plugins.test2code.*
import org.openjdk.jmh.annotations.*
import org.openjdk.jmh.annotations.Scope
import java.util.concurrent.*

@State(Scope.Benchmark)
@Fork(1)
@BenchmarkMode(Mode.Throughput)
@Warmup(iterations = 5)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Measurement(iterations = 15, timeUnit = TimeUnit.MILLISECONDS)
class CoverageBenchmark {

//    @Benchmark
//    @BenchmarkMode(Mode.AverageTime)
//    fun cyclesWithoutInstrumentation() {
//        val instrumentation = InstrumentationForTest(InvokeCycles::class.java)
//        instrumentation.runNonInstrumentedClass()
//    }
//
//    @Benchmark
//    @BenchmarkMode(Mode.AverageTime)
//    fun cyclesWithInstrumentation() {
//        val instrumentation = InstrumentationForTest(InvokeCycles::class.java)
//        instrumentation.runClass()
//    }
//
//    @Benchmark
//    @BenchmarkMode(Mode.AverageTime)
//    fun cyclesWithCollectCoverage() {
//        val instrumentation = InstrumentationForTest(InvokeCycles::class.java)
//        instrumentation.collectCoverage()
//    }

    val instrumentation = InstrumentationForTest(BenchmarkTestClass::class.java)

    @Benchmark
    @BenchmarkMode(Mode.Throughput)
    @Threads(1)
    fun conditionsWithoutInstrumentation() {
        instrumentation.runNonInstrumentedClass()
    }

    @Benchmark
    @BenchmarkMode(Mode.Throughput)
    @Threads(1)
    fun conditionsWithInstrumentation() {
        // val instrumentation = InstrumentationForTest(InvokeBigConditions::class.java)
        //  instrumentation.runClass()
        instrumentation.aa()
    }

//    @Benchmark
//    @BenchmarkMode(Mode.AverageTime)
//    fun conditionsWithCollectCoverage() {
//       // val instrumentation = InstrumentationForTest(InvokeBigConditions::class.java)
//        instrumentation.collectCoverage()
//    }

}
