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
package com.epam.drill.plugins.test2code.storage

import com.epam.drill.plugins.test2code.*
import com.epam.drill.plugins.test2code.api.*
import com.epam.drill.plugins.test2code.common.api.*
import com.epam.drill.plugins.test2code.util.*
import com.epam.kodux.*
import kotlinx.serialization.*
import kotlinx.serialization.protobuf.*

@Serializable
internal class StoredSession(
    @Id val id: String,
    val scopeId: String,
    val data: ByteArray
)

internal suspend fun StoreClient.loadSessions(
    scopeId: String
): List<FinishedSession> = findBy<StoredSession> {
    StoredSession::scopeId eq scopeId
}.map { ProtoBuf.load(FinishedSession.serializer(), Zstd.decompress(it.data)) }

internal suspend fun StoreClient.storeSession(
    scopeId: String,
    session: FinishedSession
) {
    val size =  session.probes.size / 10
    println("ProtoBuf $size")
    printMemory()
    println(session.probes.size)
    val data = ProtoBuf.dump(
        FinishedSession.serializer(),
        session
    )
    /*
        FinishedSession.serializer(),
        session.copy(probes = session.probes.chunked(size).first())
     */
    printMemory()
    store(
        StoredSession(
            id = session.id,
            scopeId = scopeId,
            data = Zstd.compress(data)
        )
    )
}

 fun printMemory() {
    val mb = 1024 * 1024

    val runtime = Runtime.getRuntime()

    val total = runtime.totalMemory()
    val freeMemory = runtime.freeMemory()

    println("Used memory " + (total - freeMemory) / mb)
    println("Free memory " + freeMemory / mb)
    println("Total memory " + total / mb)
    /** when takeMemory=100, then total~433
     *  when takeMemory=500, then total~900
     */
    /** when takeMemory=100, then total~433
     * when takeMemory=500, then total~900
     */
    println("Max memory " + runtime.maxMemory() / mb)
}

internal suspend fun StoreClient.deleteSessions(
    scopeId: String
) = deleteBy<StoredSession> {
    StoredSession::scopeId eq scopeId
}

@Serializable
data class SessionSerializableProbs(
    val id: String,
    val testType: String,
    val tests: Set<TypedTest>,
    val testStats: Map<TypedTest, TestStats> = emptyMap(),
    val probes: List<ByteArray>
)

fun FinishedSession.toSessionSerializableProbs(size:Int) =
    SessionSerializableProbs(
        id = id,
        testType = testType,
        tests = tests,
        testStats = testStats,
        probes = probes.chunked(size).map { ProtoBuf.dump(it) }
    )
