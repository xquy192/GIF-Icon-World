# Contributing

## Bug reports

Open a GitHub Issue and include:

- Minecraft version
- Fabric Loader version
- GIF Icon World version
- Operating system
- Steps to reproduce the problem
- Relevant lines from `latest.log`

For GIF-related problems, attach a small file that reproduces the issue when possible. Remove personal file paths and other private information from logs before posting them.

## Code changes

- Keep the mod client-side unless a change requires otherwise.
- Keep GIF decoding, settings, cache handling and screen code in their current packages where possible.
- Do not add a dependency for something Java or Minecraft already provides.
- Keep the GIF input limits when changing the decoder.
- Check the Minecraft 26.3 method and field signatures before changing a mixin.
- Use the existing Vietnamese terms in the in-game interface.
- Keep comments focused on decisions that are not obvious from the code.

## Build and test

On Windows, run `build.bat`. For a manual build, use JDK 25 and Gradle 9.8.2:

```sh
gradle --no-daemon clean build
```

After the build, follow the in-game checks in [docs/DEVELOPMENT.md](docs/DEVELOPMENT.md). A successful compile does not confirm that the mod works in-game.
