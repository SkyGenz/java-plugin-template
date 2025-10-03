# Template Usage Instructions

## Repo Authentication Setup
Store your nexus authentication details in your global gradle properties file (`~/.gradle/gradle.properties`):

```
arcanius_nexus_username=your_username
arcanius_nexus_password=your_password
```

## Publishing/CI Setup
On commit to the repo that uses this template, GitHub Actions will automatically build and publish the package to the Arcanius Nexus repository.
You can find the published package at: https://nexus.arcanius.net/repository/maven-releases/com/arcanius/
Make sure to update the `group` and `version` in your `build.gradle.kts` file to reflect your project's details.

## Dependency Version Management
Libraries, plugins, and groups are defined in the `gradle/libs.versions.toml` file.