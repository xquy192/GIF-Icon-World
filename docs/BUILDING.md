# Building from source

The project targets Minecraft 26.3 with Fabric Loom. Use JDK 25 and Gradle 9.8.2 for the current configuration.

## Windows helper

Run `build.bat` from the project root. It searches for a usable JDK 25+ and Gradle 9.8.2. When necessary, it downloads the tools into the current user's local application-data folder; it does not require administrator rights or modify the machine-wide `PATH`.

The helper keeps its console open on failure and writes:

- `build-helper.log` for Java and Gradle setup.
- `gradle-build.log` for Gradle output.

If a download fails checksum verification, do not bypass that check. Review the log, network/proxy access and available disk space before retrying.

## Manual build

With JDK 25 and Gradle 9.8.2 available on `PATH`:

```sh
gradle --no-daemon clean build
```

The mod JAR is written to `build/libs/`. A `-sources.jar` may also be generated for IDE inspection; it is not the file to put in `mods`.

## Version settings

Versions are declared in `gradle.properties`: `minecraft_version`, `loader_version`, `loom_version` and `mod_version`.

Minecraft 26.3 uses the unobfuscated game artifacts in this project configuration. Do not add Yarn or Mojang mapping coordinates copied from older Minecraft templates without checking the 26.3 toolchain.

## IDE

Import the folder as a Gradle project and select JDK 25 as the Gradle JVM and project SDK. Let Loom resolve dependencies before running `build`.

## Continuous integration

GitHub Actions builds pushes and pull requests using Temurin 25 and Gradle 9.8.2. The produced JARs are uploaded as workflow artifacts; CI does not publish a release automatically.
