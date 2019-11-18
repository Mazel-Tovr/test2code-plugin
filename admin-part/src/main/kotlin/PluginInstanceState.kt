package com.epam.drill.plugins.coverage

import com.epam.drill.common.*
import com.epam.drill.plugins.coverage.storage.*
import com.epam.kodux.*
import kotlinx.atomicfu.*
import org.jacoco.core.analysis.*
import org.jacoco.core.data.*

/**
 * Agent state.
 * The state itself holds only an atomic reference to the data.
 * The data is represented by the sealed class hierarchy AgentData.
 * In case of inconsistencies of the data a ClassCastException is thrown.
 */

class PluginInstanceState(
    val storeClient: StoreClient,
    val prevBuildVersion: String,
    val lastPrevBuildCoverage: Double,
    val agentInfo: AgentInfo,
    val testsAssociatedWithBuild: TestsAssociatedWithBuild
) {
    @Suppress("PropertyName")
    private val _data = atomic<AgentData>(NoData)

    var data: AgentData
        get() = _data.value
        private set(value) {
            _data.value = value
        }

    suspend fun setLastBuildCoverage(value: Double) {
        storeClient.store(
            LastBuildCoverage(
                id = lastCoverageId(agentInfo.id, agentInfo.buildVersion),
                coverage = value
            )
        )
    }

    val scopeManager = ScopeManager(storeClient)

    private val _scopeCounter = atomic(0)

    private val _activeScope = atomic(ActiveScope(scopeName(), agentInfo.buildVersion))

    val activeScope get() = _activeScope.value

    fun init() {
        _data.updateAndGet { ClassDataBuilder }
    }

    suspend fun nextVersion(buildVersion: String): String {
        val versionMap = scopeManager.getVersionMap()
        return versionMap[buildVersion] ?: ""
    }

    suspend fun renameScope(id: String, newName: String) {
        val trimmedNewName = newName.trim()
        if (id == activeScope.id) activeScope.rename(trimmedNewName)
        else scopeManager.getScope(id)?.apply {
            scopeManager.saveScope(this.copy(name = newName, summary = this.summary.copy(name = trimmedNewName)))
        }
    }

    suspend fun toggleScope(id: String) {
        scopeManager.getScope(id)?.apply {
            scopeManager.saveScope(this.copy(enabled = !enabled, summary = this.summary.copy(enabled = !enabled)))
        }
    }

    suspend fun scopeNameNotExisting(name: String, buildVersion: String) =
        scopeManager.scopesByBuildVersion(buildVersion)
            .find { it.name == name.trim() } == null && (name.trim() != activeScope.name || agentInfo.buildVersion != buildVersion)

    suspend fun scopeNotExisting(id: String) = scopeManager.getScope(id) == null && activeScope.id != id


    suspend fun initialized(buildInfos: Map<String, BuildInfo>) {
        val buildInfo = buildInfos[agentInfo.buildVersion]
        val classesBytes = buildInfo?.classesBytes ?: emptyMap()
        val coverageBuilder = CoverageBuilder()
        val analyzer = Analyzer(ExecutionDataStore(), coverageBuilder)
        classesBytes.forEach { analyzer.analyzeClass(it.value, it.key) }
        val bundleCoverage = coverageBuilder.getBundle("")

        val classesData = ClassesData(
            buildVersion = agentInfo.buildVersion,
            totalInstructions = bundleCoverage.instructionCounter.totalCount,
            prevBuildVersion = prevBuildVersion,
            prevBuildCoverage = lastPrevBuildCoverage
        )

        data = classesData
        scopeManager.saveClassesData(classesData)
    }

    //throw ClassCastException if the ref value is in the wrong state
    suspend fun classesData(buildVersion: String = agentInfo.buildVersion): AgentData =
        if (buildVersion == agentInfo.buildVersion) {
            data as ClassesData
        } else scopeManager.classesData(buildVersion) ?: NoData

    fun changeActiveScope(name: String) =
        _activeScope.getAndUpdate { ActiveScope(scopeName(name), agentInfo.buildVersion) }

    private fun scopeName(name: String = "") = when (val trimmed = name.trim()) {
        "" -> "New Scope ${_scopeCounter.incrementAndGet()}"
        else -> trimmed
    }
}