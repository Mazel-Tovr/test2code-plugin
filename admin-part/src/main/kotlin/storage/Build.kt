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
package com.epam.drill.plugins.test2code.storage

import com.epam.drill.plugins.test2code.*
import com.epam.drill.plugins.test2code.api.*
import com.epam.drill.plugins.test2code.coverage.*
import com.epam.drill.plugins.test2code.util.*
import com.epam.kodux.*
import kotlinx.serialization.*
import kotlinx.serialization.protobuf.*

@Serializable
internal class StoredClassData(
    @Id val version: String,
    val data: ByteArray
)

@Serializable
internal class StoredBundles(
    @Id val version: String,
    val data: ByteArray
)

@Serializable
class StoredBuildTests(
    @Id val version: String,
    val data: ByteArray
)

internal suspend fun StoreClient.loadClassData(
    version: String
): ClassData? = findById<StoredClassData>(version)?.run {
    ProtoBuf.load(ClassData.serializer(), Zstd.decompress(data)).let { classData: ClassData ->
        classData.copy(
            buildVersion = classData.buildVersion.intern(),
            packageTree = classData.packageTree.let { packageTree ->
                packageTree.copy(packages = packageTree.packages.map { javaPackageCoverage: JavaPackageCoverage ->
                    javaPackageCoverage.copy(
                        id = javaPackageCoverage.id.intern(),
                        name = javaPackageCoverage.name.intern(),
                        classes = javaPackageCoverage.classes.map { javaClassCoverage ->
                            javaClassCoverage.copy(
                                id = javaClassCoverage.id.intern(),
                                name = javaClassCoverage.name.intern(),
                                path = javaClassCoverage.path.intern(),
                                methods = javaClassCoverage.methods.map { javaMethodCoverage ->
                                    javaMethodCoverage.copy(
                                        id = javaMethodCoverage.id.intern(),
                                        name = javaMethodCoverage.name.intern(),
                                        desc = javaMethodCoverage.desc.intern(),
                                        decl = javaMethodCoverage.decl.intern(),
                                    )
                                }
                            )
                        }
                    )
                })
            }
        )
    }
}

internal suspend fun ClassData.store(storage: StoreClient) {
    val stored = ProtoBuf.dump(ClassData.serializer(), this)
    storage.store(StoredClassData(buildVersion, Zstd.compress(stored)))
}

internal suspend fun StoreClient.loadBuild(
    version: String
): CachedBuild? = findById<BuildStats>(version)?.let { stats ->
    CachedBuild(
        version = version.intern(),
        stats = stats.copy(coverageByType = stats.coverageByType.mapKeys { it.key.intern() }),
        bundleCounters = findById<StoredBundles>(version)?.run {
            ProtoBuf.load(BundleCounters.serializer(), Zstd.decompress(data))
        }?.copyIntern() ?: BundleCounters.empty,
        tests = findById<StoredBuildTests>(version)?.run {
            ProtoBuf.load(BuildTests.serializer(), Zstd.decompress(data))
        }?.copyIntern() ?: BuildTests()
    )
}

internal suspend fun CachedBuild.store(storage: StoreClient) {
    storage.executeInAsyncTransaction {
        val bundleData = ProtoBuf.dump(BundleCounters.serializer(), bundleCounters)
        val testData = ProtoBuf.dump(BuildTests.serializer(), tests)
        store(stats)
        store(StoredBundles(version, Zstd.compress(bundleData)))
        store(StoredBuildTests(version, Zstd.compress(testData)))
    }
}
