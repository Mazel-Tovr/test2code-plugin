package com.epam.drill.plugins.test2code.common.api

import com.epam.drill.plugins.test2code.*

actual typealias Probes = SupperCustomBitset

class ProbesStub(size: Int = 0) : SupperCustomBitset(size) {
    override fun set(bitIndex: Int) {}

    override fun set(bitIndex: Int, value: Boolean) {}
}
