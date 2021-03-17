import com.epam.drill.plugins.test2code.api.*
import com.epam.drill.plugins.test2code.coverage.*
import com.epam.drill.plugins.test2code.util.*
import kotlinx.serialization.*
import kotlinx.serialization.protobuf.*
import java.util.*
import kotlin.test.*

class TestStringDeduplication {

    @Test
    fun `test jvm ags`() {
        val strings = ArrayList<String>(9999999)

        for (i in 0..9999999) {
            //String()
            // val sb = StringBuilder()
            // sb.append("omygush")
            //          strings.add(sb.toString().intern());
            val s = String("omygush".toByteArray())
            strings.add(s)
        }
        println(Runtime.getRuntime().totalMemory())

        System.gc()
        while (true)
            Thread.sleep(100)
    }


    @Test
    fun `test intern`() {
        val strings = ArrayList<String>(9999999)

        for (i in 0..9999999) {
            val s = String("omygush".toByteArray()).intern()
            strings.add(s)
        }
        println(Runtime.getRuntime().totalMemory())

        System.gc()
        while (true)
            Thread.sleep(100)
    }

    @Test
    fun `test intern on tree structure`() {
        var bundle: ArrayList<BundleCounter>? = ArrayList<BundleCounter>(999999)

        for (i in 0..999999) {

            val bundleCounter = BundleCounter(
                name = "", packages = listOf(
                    PackageCounter(
                        "Package",
                        zeroCount,
                        zeroCount,
                        zeroCount,
                        listOf(
                            ClassCounter(
                                "Path",
                                "ClassCounter",
                                zeroCount,
                                listOf(
                                    MethodCounter(
                                        "",
                                        "desc",
                                        "desc",
                                        zeroCount
                                    )
                                )
                            )
                        )
                    )
                )
            )
//            val bundleCounter = BundleCounter(
//                name = "", packages = listOf(
//                    PackageCounter(
//                        String("Package".toByteArray()),
//                        zeroCount,
//                        zeroCount,
//                        zeroCount,
//                        listOf(
//                            ClassCounter(
//                                String("Path".toByteArray()),
//                                String("ClassCounter".toByteArray()),
//                                zeroCount,
//                                listOf(
//                                    MethodCounter(
//                                        "",
//                                        String("desc".toByteArray()),
//                                        String("desc".toByteArray()),
//                                        zeroCount
//                                    )
//                                )
//                            )
//                        )
//                    )
//                )
//            )
            bundle!!.add(bundleCounter)
        }
        println(Runtime.getRuntime().totalMemory())

        val deserialized = ProtoBuf.load(
            ArrayWrapper.serializer(),
            ProtoBuf.dump(ArrayWrapper.serializer(), ArrayWrapper(bundle!!))
        )
        bundle = null
        System.gc()
        while (true) {
            deserialized.bundle.size
            Thread.sleep(1000)
        }
    }

    @Serializable
    data class ArrayWrapper(val bundle: ArrayList<BundleCounter>)

}
