# MongoDB Client Reuse Companion

Warning icon on a `new MongoClient(...)` (legacy driver) or
`MongoClients.create(...)` (modern `com.mongodb.client` driver)
construction written inside a regular method body — MongoDB's own
documentation states "you will only need one instance of class
MongoClient even with multiple threads", and that not following the
singleton pattern "may lead to too many open connections in MongoDB".
A MongoClient holds an internal connection pool (default size 100) —
building one inside a regular method means a brand new pool on every
call.

## Why it exists

`new MongoClient(...)` compiles fine and returns a working client —
call it once per request handler and each call quietly opens up to
100 new connections, exhausting the connection budget under load
instead of reusing the one pool the application actually needs.

## Why built this way

- **100% static text/PSI analysis** — matches the class name by simple
  text, so it works whether the real MongoDB driver jar is on the
  classpath or not. Java and Kotlin.

## v0.1 scope — stated honestly, not exhaustively

Only flags the "build from scratch" shape — a client obtained by
reference from an existing shared instance/dependency injection is
never flagged (correctly, since it isn't the anti-pattern this plugin
targets).

## Usage

Open any Java/Kotlin file using the MongoDB driver. A client built
inside a regular method shows a warning icon.

## Enterprise / Team Licensing

Need enterprise features, custom rules, or team licensing? Contact us at
**gaphunterlabs@gmail.com**.

## Development

```
./gradlew test           # unit tests
./gradlew buildPlugin    # generates build/distributions/*.zip
./gradlew verifyPlugin   # checks compatibility against real IDEs
```

## License

Apache-2.0. See `LICENSE`.
