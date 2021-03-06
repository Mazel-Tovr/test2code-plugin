package com.epam.drill.plugins.test2code

import com.epam.drill.plugins.test2code.jvm.*
import com.epam.drill.plugins.test2code.util.*
import org.jacoco.core.analysis.*
import kotlin.test.*

class JacocoExtTest {

    @Test
    fun `crcr64 should return same values for equal strings`() {
        val str1 = javaClass.name.substringAfterLast(".")
        val str2 = javaClass.simpleName
        assertNotSame(str1, str2) //the strings should not be identical
        assertEquals(str1, str2)
        assertEquals(str1.crc64, str2.crc64)
    }

    @Test
    fun `coverage should return zero for non-finite ratios`() {
        val coverageNode = CoverageNodeImpl(ICoverageNode.ElementType.METHOD, "test")
        val counter = coverageNode.methodCounter
        assertEquals(0, counter.totalCount)
        assertFalse { counter.coveredRatio.isFinite() }
        assertEquals(0.0, coverageNode.coverage())
    }

    @Test
    fun `should convert V to void`() {
        val asmVoid = "V"
        val convertedVoid = parseDescType(asmVoid[0], asmVoid.iterator())
        assertEquals("void", convertedVoid)
    }

    @Test
    fun `should covert ASM declaration of array to Java declaration`() {
        val asmDesc = "[Ljava/lang/Integer;"
        val convertedDesc = parseDescTypes(asmDesc).first()
        assertEquals("Integer[]", convertedDesc)
    }

    @Test
    fun `should covert ASM declaration of method to Java declaration`() {
        CoverageNodeImpl(ICoverageNode.ElementType.METHOD, "test")
        val asmDesc = "([JLjava/lang/Integer;[I)Z"
        val convertedDesc = declaration(asmDesc)
        assertEquals("(long[], Integer, int[]): boolean", convertedDesc)
    }
}
