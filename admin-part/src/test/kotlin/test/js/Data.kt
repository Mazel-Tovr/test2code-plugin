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
package com.epam.drill.plugins.test2code.test.js

import com.epam.drill.common.*
import com.epam.drill.plugins.test2code.common.api.*

val jsAgentInfo = AgentInfo(
    id = "jsag",
    name = "jsag",
    description = "",
    buildVersion = "0.1.0",
    agentType = "NODEJS",
    agentVersion = ""
)

val ast = listOf(
    AstEntity(
        path = "foo/bar",
        name = "baz.js",
        methods = listOf(
            AstMethod(
                name = "foo",
                params = listOf("one", "two"),
                returnType = "number",
                count = 2,
                probes = listOf(1, 3)
            ),
            AstMethod(
                name = "bar",
                params = listOf(),
                returnType = "void",
                count = 1,
                probes = listOf(6)
            ),
            AstMethod(
                name = "baz",
                params = listOf(),
                returnType = "void",
                count = 2,
                probes = listOf(7, 8)
            )

        )
    )
)

val probes = listOf(
    ExecClassData(
        className = "foo/bar/baz.js",
        testName = "default",
        probes = booleanArrayOf(true, true, false, true, false).toBitSet()
    )
)

object IncorrectProbes {
    val overCount = listOf(
        ExecClassData(
            className = "foo/bar/baz.js",
            testName = "default",
            probes = booleanArrayOf(true, true, false, true, false, /*extra*/ false).toBitSet()
        )
    )

    val underCount = listOf(
        ExecClassData(
            className = "foo/bar/baz.js",
            testName = "default",
            probes = booleanArrayOf(true, true, false, true).toBitSet()
        )
    )

    val notExisting = listOf(
        ExecClassData(
            className = "foo/bar/not-existing",
            testName = "default",
            probes = booleanArrayOf(false, false).toBitSet()
        )
    )
}

