package com.epam.drill.plugins.coverage

import com.epam.drill.plugin.api.*
import kotlinx.serialization.*
import kotlinx.serialization.modules.*

@SharedImmutable
val commonSerDe = SerDe(
    actionSerializer = Action.serializer(),
    ctx = SerializersModule {
        polymorphic(Action::class) {
            StartNewSession::class with StartNewSession.serializer()
            StartSession::class with StartSession.serializer()
            StopSession::class with StopSession.serializer()
            CancelSession::class with CancelSession.serializer()
            SwitchScope::class with SwitchScope.serializer()
            IgnoreScope::class with IgnoreScope.serializer()
            DropScope::class with DropScope.serializer()
        }
    }
)