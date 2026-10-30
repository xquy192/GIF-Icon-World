# Source distribution

This repository contains the editable project source and the Windows build helper. The standalone source ZIP is a convenience copy; the files in the repository are the place to browse changes and submit contributions.

Build output is not committed. Run `build.bat` on Windows, or use `gradle --no-daemon clean build` with JDK 25 and Gradle 9.8.2. The JAR is created in `build/libs/`.
