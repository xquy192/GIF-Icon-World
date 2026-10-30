# Contributing

## Reporting a bug

Open a GitHub issue with the Minecraft version, Fabric Loader version, mod version, operating system and steps to reproduce the problem. Include the relevant lines from `latest.log`.

For GIF-related bugs, attach a small file that reproduces the problem if possible. Remove personal paths and other private information from logs first.

## Code changes

Keep client code in the client source set. Check the Minecraft 26.3 signatures before changing a mixin. Reuse the existing GIF limits and Vietnamese labels. Avoid adding a dependency when Java or Minecraft already provides what the code needs.

## Build and test

On Windows, run `build.bat`. To build manually, use JDK 25 and Gradle 9.8.2:

```sh
gradle --no-daemon clean build
```

After building, run through the checks in [docs/DEVELOPMENT.md](docs/DEVELOPMENT.md). A successful compile does not confirm that the mod works in-game.
