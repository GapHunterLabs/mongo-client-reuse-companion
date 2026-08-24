package com.acmecorp.orders;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;

/**
 * Demo data for MongoDB Client Reuse Companion — used with
 * `./gradlew runIde` to capture the real Marketplace screenshot. Open
 * this file, the warning icon should appear on the call inside
 * `findOrder`.
 */
public class OrderRepository {

    private final MongoClient sharedClient;

    public OrderRepository() {
        // Built once, in the constructor -- NOT flagged.
        this.sharedClient = MongoClients.create("mongodb://localhost:27017");
    }

    public Object findOrder(String id) {
        // Built here on every call -- a fresh connection pool each
        // time. FLAGGED.
        MongoClient client = MongoClients.create("mongodb://localhost:27017");
        return client.getDatabase("orders").getCollection("orders").find();
    }
}
