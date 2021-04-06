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
import com.epam.drill.plugins.test2code.util.*
import com.epam.kodux.*
import kotlinx.atomicfu.*
import kotlinx.collections.immutable.*
import kotlinx.serialization.*

@Serializable
internal data class GlobalAgentData(
    @Id val agentId: String,
    val baseline: Baseline = Baseline()
)

@Serializable
internal data class Baseline(
    val version: String = "",
    val parentVersion: String = ""
)

internal sealed class AgentData

internal object NoData : AgentData()

internal class DataBuilder : AgentData(), Iterable<AstEntity> {

    private val _data = atomic(persistentListOf<AstEntity>())

    operator fun plusAssign(parts: Iterable<AstEntity>) = _data.update { it + parts }

    override fun iterator() = _data.value.iterator()
}

@Serializable
internal data class ClassData(
    @StringIntern
    @Id val buildVersion: String,
    val packageTree: PackageTree = emptyPackageTree,
    val methods: List<Method> = emptyList(),
    val probeIds: Map<String, Long> = emptyMap()
) : AgentData(), java.io.Serializable {
    companion object {
        private val emptyPackageTree = PackageTree()
    }

    override fun equals(other: Any?) = other is ClassData && buildVersion == other.buildVersion

    override fun hashCode() = buildVersion.hashCode()
}
