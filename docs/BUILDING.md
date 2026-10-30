# Building from source

The project targets Minecraft 26.3. Build it with JDK 25 and Gradle 9.8.2.

## Windows

Run `build.bat` from the project folder. It searches the script's folder and its parent folders for the project files, so the script can be renamed or kept in a subfolder. If you keep it outside the project, set `GIF_ICON_WORLD_PROJECT` to the project folder. The script looks for an installed JDK 25 or newer and Gradle 9.8.2. If it cannot find them, it downloads Temurin and Gradle into `%LOCALAPPDATA%\GifIconWorldBuild`. It does not change the system-wide `PATH`.

The console stays open if the build fails. Check these logs:

- `build-helper.log` for Java and Gradle setup.
- `gradle-build.log` for Gradle output.

The JDK archive is checked against the SHA-256 value from Adoptium before extraction.

## Manual build

Make JDK 25 and Gradle 9.8.2 available on `PATH`, then run:

```sh
gradle --no-daemon clean build
```

The mod JAR is written to `build/libs/`. Gradle may also create a `-sources.jar`; use the regular JAR in Minecraft.

## Version settings

The Minecraft, loader, Loom and mod versions are set in `gradle.properties`.

The 26.3 build uses the game artifacts configured by Loom. Do not copy mappings entries from an older Minecraft project into this build.

## IDE

Import the folder as a Gradle project and set JDK 25 as the project SDK and Gradle JVM. Let Loom resolve the dependencies before running a build.

## GitHub Actions

The workflow builds pushes and pull requests with Temurin 25 and Gradle 9.8.2. It uploads the JARs as workflow artifacts. It does not publish a Modrinth or GitHub release.
