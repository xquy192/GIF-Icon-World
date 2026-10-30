# Building from source

The project targets Minecraft 26.3 and uses Fabric Loom. The current build requires JDK 25 and Gradle 9.8.2.

## Windows

Run `build.bat` from the project root. The script looks for a usable JDK 25 or newer and Gradle 9.8.2. If either is missing, it downloads the tool into the current Windows user's local application-data folder. It does not need administrator rights or change the system-wide `PATH`.

The console stays open if a step fails. The helper writes two logs:

- `build-helper.log`: Java and Gradle setup.
- `gradle-build.log`: Gradle output.

If an archive fails its SHA-256 check, do not skip the check. Review the log, network or proxy settings, and available disk space before trying again.

## Manual build

Make sure JDK 25 and Gradle 9.8.2 are available on `PATH`, then run:

```sh
gradle --no-daemon clean build
```

The mod JAR is written to `build/libs/`. A `-sources.jar` may also be created for IDE use; install the regular mod JAR in Minecraft.

## Version settings

The versions are in `gradle.properties`:

- `minecraft_version`
- `loader_version`
- `loom_version`
- `mod_version`

This project uses the Minecraft 26.3 game artifacts configured in Gradle. Do not copy Yarn or Mojang mapping coordinates from an older Minecraft template without checking that they are supported by the 26.3 toolchain.

## IDE

Import the project as a Gradle project. Set JDK 25 as both the project SDK and the Gradle JVM, then let Loom resolve the dependencies before running the build.

## GitHub Actions

The workflow builds pushes and pull requests with Temurin 25 and Gradle 9.8.2. It uploads the resulting JARs as workflow artifacts. It does not publish a Modrinth or GitHub release.
