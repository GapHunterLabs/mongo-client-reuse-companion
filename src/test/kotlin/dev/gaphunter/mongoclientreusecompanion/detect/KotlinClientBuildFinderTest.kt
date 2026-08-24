package dev.gaphunter.mongoclientreusecompanion.detect

import com.intellij.testFramework.fixtures.BasePlatformTestCase

class KotlinClientBuildFinderTest : BasePlatformTestCase() {

    fun `test new MongoClient inside a regular method is flagged`() {
        val file = myFixture.configureByText(
            "OrderRepository.kt",
            """
            class OrderRepository {
                fun findOrder(id: String): Any {
                    val client = MongoClient("localhost", 27017)
                    return client.getDatabase("orders").getCollection("orders").find()
                }
            }
            """.trimIndent(),
        )
        assertEquals(1, KotlinClientBuildFinder.findAll(file).size)
    }

    fun `test MongoClients-create inside a regular method is flagged`() {
        val file = myFixture.configureByText(
            "OrderRepository.kt",
            """
            class OrderRepository {
                fun findOrder(id: String): Any {
                    val client = MongoClients.create("mongodb://localhost:27017")
                    return client.getDatabase("orders").getCollection("orders").find()
                }
            }
            """.trimIndent(),
        )
        assertEquals(1, KotlinClientBuildFinder.findAll(file).size)
    }

    fun `test construction inside a class initializer is not flagged`() {
        val file = myFixture.configureByText(
            "OrderRepository.kt",
            """
            class OrderRepository {
                private val client = MongoClient("localhost", 27017)
            }
            """.trimIndent(),
        )
        assertTrue(KotlinClientBuildFinder.findAll(file).isEmpty())
    }

    fun `test unrelated class construction is not flagged`() {
        val file = myFixture.configureByText(
            "OrderRepository.kt",
            """
            class OrderRepository {
                fun build(): Any {
                    return StringBuilder()
                }
            }
            """.trimIndent(),
        )
        assertTrue(KotlinClientBuildFinder.findAll(file).isEmpty())
    }
}
