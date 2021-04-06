package com.epam.drill.plugins.test2code.util

import kotlinx.serialization.Serializable
import kotlinx.serialization.protobuf.*
import org.nustaq.serialization.*
import java.io.*
import kotlin.time.*


val conf = FSTConfiguration.getDefaultConfiguration()

inline fun <reified T> dump(
    value: T,
): ByteArray = conf.asByteArray(value)

inline fun <reified T> load(
    bytes: ByteArray,
): T = conf.asObject(bytes) as T


fun main() {
    val listWrapper = ListWrapper(mutableListOf())
    for (z in 1..3000) {
        val a = FstList(z, mutableListOf())
        for (i in 1..10_000) {
            a.list.add(GGWP("$i", i))
        }
        listWrapper.list.add(a)
    }
    val dump = measureTimedValue {
        dump(listWrapper)
    }.apply { println("FST Dump duration ${duration.inSeconds}") }.value
    val restore = measureTimedValue {
        val l: ListWrapper = load(dump)
        l
    }.apply { println("FST Load duration ${duration.inSeconds}") }.value
    //println("restore")

    val outputBytesStream = ByteArrayOutputStream()
    val out = ObjectOutputStream(outputBytesStream)

    val dumpJava = measureTimedValue {
        out.writeObject(listWrapper)
        out.flush()
        outputBytesStream.toByteArray()
    }.apply { println("Java Dump time ${duration.inSeconds}") }.value

    val byteArrayInputStream = ByteArrayInputStream(dumpJava)
    val ob = ObjectInputStream(byteArrayInputStream)
    val loadJava = measureTimedValue {
        ob.readObject() as? ListWrapper
    }.apply { println("Java Load time ${duration.inSeconds}") }.value

    val dump1 = measureTimedValue {
         ProtoBuf.dump(ListWrapper.serializer(),listWrapper)
    }.apply { println("Kotlin Dump duration ${duration.inSeconds}") }.value
    val restore1 = measureTimedValue {
        ProtoBuf.load(ListWrapper.serializer(),dump1)

    }.apply { println("Kotlin Load duration ${duration.inSeconds}") }.value

}

@Serializable
data class ListWrapper(val list: MutableList<FstList>) : java.io.Serializable
@Serializable
data class FstList(val id: Int, val list: MutableList<GGWP>) : java.io.Serializable
@Serializable
data class GGWP(val string: String, val int: Int) : java.io.Serializable
