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
package com.epam.drill.plugins.test2code.jvm

import com.epam.drill.plugins.test2code.Method
import com.epam.drill.plugins.test2code.coverage.*
import com.epam.drill.plugins.test2code.util.*
import org.apache.bcel.classfile.*
import java.io.*

internal fun ClassCounter.parseMethods(classBytes: ByteArray): List<Method> = run {
    val classParser = ClassParser(ByteArrayInputStream(classBytes), fullName)
    val parsedMethods = classParser.parse().run {
        methods.associateBy { it.name to it.signature }
    }
    methods.map { m ->
        val method = parsedMethods[m.name to m.desc]
        Method(
            ownerClass = fullName.intern(),
            name = m.name.intern(),
            desc = m.desc.intern(),
            hash = method.checksum().intern()
        )
    }
}

private fun org.apache.bcel.classfile.Method?.checksum(): String = (this?.code?.run {
    Utility.codeToString(code, constantPool, 0, length, false)
} ?: "").crc64
