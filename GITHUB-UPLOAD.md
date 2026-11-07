# Upload and share TV Dimmer

The GitHub-ready ZIP has repository files at its top level. Extract it first. Upload the extracted contents, not the ZIP itself and not an extra enclosing directory.

## Upload using your browser

1. Create a GitHub repository. Choose Public if you want other people to see the source.
2. Open the repository and use Add file > Upload files. Upload app, gradle, docs, gradlew, gradlew.bat, build.gradle, settings.gradle and the Markdown files. Keep their directory structure.
3. Include .gitignore and .github/workflows/build.yml. If the browser refuses hidden files/folders, create those two paths directly with Add file > Create new file. The full build workflow is copied below for convenience.
4. Commit to main. Open Actions > Build debug APK and run the workflow if a build has not already started.
5. Download the tv-dimmer-debug-apk artifact from a successful run.

The repository root must contain settings.gradle and app/build.gradle, not tv-dimmer/settings.gradle one directory deeper.

## Share an installable APK

Create a GitHub Release and attach the supplied APK as a release asset, then share the release URL. This is separate from sharing the source repository. The build workflow produces test APKs; a stable private signing key is needed for durable release updates.

The supplied APK and APKs built by your GitHub workflow can use different signing keys. Signing keys are not included in this project ZIP.

## Build workflow

Create this exact filename: .github/workflows/build.yml

```yaml
name: Build debug APK
on:
  workflow_dispatch:
  push:
    branches: [main]
jobs:
  apk:
    runs-on: ubuntu-latest
    permissions:
      contents: read
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v4
        with:
          distribution: temurin
          java-version: '17'
      - uses: actions/cache@v4
        with:
          path: ~/.android/debug.keystore
          key: tv-dimmer-debug-keystore-v1
      - uses: android-actions/setup-android@v3
        with:
          packages: 'platform-tools'
      - uses: gradle/actions/setup-gradle@v4
        with:
          gradle-version: '8.9'
      - run: sdkmanager 'platforms;android-35' 'build-tools;35.0.0'
      - run: gradle --no-daemon :app:testDebugUnitTest :app:assembleDebug :app:lintDebug
      - uses: actions/upload-artifact@v4
        with:
          name: tv-dimmer-debug-apk
          path: app/build/outputs/apk/debug/app-debug.apk
```

## Official GitHub help

- https://docs.github.com/en/repositories/working-with-files/managing-files/adding-a-file-to-a-repository
- https://docs.github.com/en/repositories/working-with-files/managing-files/creating-new-files
- https://docs.github.com/en/repositories/releasing-projects-on-github/about-releases
