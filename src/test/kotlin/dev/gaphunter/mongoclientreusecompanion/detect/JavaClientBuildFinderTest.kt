package dev.gaphunter.mongoclientreusecompanion.detect

import com.intellij.testFramework.fixtures.BasePlatformTestCase

class JavaClientBuildFinderTest : BasePlatformTestCase() {

    fun `test new MongoClient inside a regular method is flagged`() {
        val file = myFixture.configureByText(
            "OrderRepository.java",
            """
            class OrderRepository {
                Object findOrder(String id) {
                    MongoClient client = new MongoClient("localhost", 27017);
                    return client.getDatabase("orders").getCollection("orders").find();
                }
            }
            """.trimIndent(),
        )
        assertEquals(1, JavaClientBuildFinder.findAll(file).size)
    }

    fun `test MongoClients-create inside a regular method is flagged`() {
        val file = myFixture.configureByText(
            "OrderRepository.java",
            """
            class OrderRepository {
                Object findOrder(String id) {
                    MongoClient client = MongoClients.create("mongodb://localhost:27017");
                    return client.getDatabase("orders").getCollection("orders").find();
                }
            }
            """.trimIndent(),
        )
        assertEquals(1, JavaClientBuildFinder.findAll(file).size)
    }

    fun `test construction inside a constructor is not flagged`() {
        val file = myFixture.configureByText(
            "OrderRepository.java",
            """
            class OrderRepository {
                private final MongoClient client;

                OrderRepository() {
                    this.client = new MongoClient("localhost", 27017);
                }
            }
            """.trimIndent(),
        )
        assertTrue(JavaClientBuildFinder.findAll(file).isEmpty())
    }

    fun `test unrelated class construction is not flagged`() {
        val file = myFixture.configureByText(
            "OrderRepository.java",
            """
            class OrderRepository {
                Object build() {
                    return new StringBuilder();
                }
            }
            """.trimIndent(),
        )
        assertTrue(JavaClientBuildFinder.findAll(file).isEmpty())
    }
}
