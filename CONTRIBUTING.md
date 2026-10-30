# Contributing

Thanks for considering a contribution. Small, focused changes are easier to review and test.

## Bug reports

Open a GitHub Issue and include the Minecraft version, Fabric Loader version, mod version, operating system, steps to reproduce, expected result and relevant `latest.log` lines. For GIF-specific issues, include a small test GIF when possible. Remove private paths or other personal information before posting logs.

## Code changes

- Keep the client-only scope unless there is a clear reason to change it.
- Keep decoding, settings persistence, cache management and UI code in their existing packages where practical.
- Avoid adding dependencies for work already supported by Java or Minecraft.
- Keep GIF input limits in place when changing the decoder.
- Check exact Minecraft 26.3 signatures before modifying mixins.
- Keep UI wording consistent with the existing Vietnamese interface.
- Use names and comments that explain intent without restating the code.

## Build and test

Use `build.bat` on Windows, or `gradle --no-daemon clean build` with JDK 25 and Gradle 9.8.2. Then follow the checklist in [docs/DEVELOPMENT.md](docs/DEVELOPMENT.md). A successful compile does not replace an in-game test.
