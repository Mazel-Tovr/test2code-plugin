package com.epam.drill.plugins.test2code.common.api

import kotlinx.serialization.*

@Serializable
sealed class CoverMessage

@SerialName("INIT")
@Serializable
data class InitInfo(
    val classesCount: Int = 0,
    val message: String = "",
    val init: Boolean = false
) : CoverMessage()

@SerialName("INIT_DATA_PART")
@Serializable
data class InitDataPart(val astEntities: List<AstEntity>) : CoverMessage()

@SerialName("INITIALIZED")
@Serializable
data class Initialized(val msg: String = "") : CoverMessage()

@SerialName("SCOPE_INITIALIZED")
@Serializable
data class ScopeInitialized(
    val id: String,
    val name: String,
    val prevId: String,
    val ts: Long
) : CoverMessage()

@SerialName("SESSION_STARTED")
@Serializable
data class SessionStarted(
    val sessionId: String,
    val testType: String,
    val isRealtime: Boolean = false,
    val ts: Long
) : CoverMessage()

@SerialName("SESSION_CANCELLED")
@Serializable
data class SessionCancelled(val sessionId: String, val ts: Long) : CoverMessage()

@SerialName("ALL_SESSIONS_CANCELLED")
@Serializable
data class AllSessionsCancelled(val ids: List<String>, val ts: Long) : CoverMessage()

@SerialName("COVERAGE_DATA_PART")
@Serializable
data class CoverDataPart(val sessionId: String, val data: List<ExecClassData>) : CoverMessage()

@SerialName("SESSION_CHANGED")
@Serializable
data class SessionChanged(val sessionId: String, val probeCount: Int) : CoverMessage()

@SerialName("SESSION_FINISHED")
@Serializable
data class SessionFinished(val sessionId: String, val ts: Long) : CoverMessage()