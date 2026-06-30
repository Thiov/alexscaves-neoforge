# Alex's Caves — NeoForge 26.1.2

Unofficial NeoForge port of **Alex's Caves** for Minecraft **26.1.2** (NeoForge 26.1.2.75).
Ported from the Fabric build back to native NeoForge APIs. This build requires the **Citadel** library installed separately (the same standalone Citadel that Alex's Mobs uses), so Alex's Caves and Alex's Mobs can be installed together.

Original mod by Alexthe668 and Noonyeyz. This NeoForge port is maintained by Thiov and is not
affiliated with or endorsed by the original authors.

## Build
```
./gradlew build        # produces build/libs/alexscaves-<version>-neoforge-26.1.2.jar
./gradlew runServer    # dedicated server (verified loads & generates a world)
./gradlew runClient    # client
```
Requires a JDK 21+ launcher; the Gradle toolchain provisions JDK 25 for compilation.

## License
LGPL-3.0-only. Citadel is a separate required dependency that you install yourself; it is not bundled.
