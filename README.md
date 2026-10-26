# People Directory

A small Android directory app: pick a role — Student, Teacher, or Administrator — fill in the form, and the person appears in the list. Entries are saved on the device, so they survive rotation and app restarts.

This started as a Full Sail CE05 assignment ("Data Display Fragments"). Version 2.0.0 is a ground-up modernization of that app, keeping its behavior and fixing its bugs — see [CHANGELOG.md](CHANGELOG.md) for the full list.

## What it does

- Three roles, each with its own field: students have an ID number, teachers a course, administrators a program.
- The list shows every person with their name, role, and that role-specific field — the original app collected the field but never displayed it.
- Adding a person updates the list immediately. No Refresh button, and switching roles keeps each role's unsaved draft instead of discarding it.
- Blank or whitespace-only fields are rejected with inline errors, and input is trimmed.
- People can be deleted from the list.
- Works in portrait and landscape. On wide screens the form and list sit side by side; on narrow screens they stack.

No network, no permissions, no accounts, no tracking. Everything lives in a local Room database.

## Tech stack

- Kotlin 2.2.0, Jetpack Compose Material 3
- ViewModel + StateFlow, Room for persistence
- AGP 8.13.0, Gradle 8.14.3, Java 17
- Compile/target SDK 36, min SDK 24
- Kotlin DSL with a version catalog, R8 for release builds
- GitHub Actions CI: JVM unit tests + debug build on every push to `main`

## Project structure

```text
app/src/main/java/com/enonymous1/peopledirectory/
├── MainActivity.kt            # Builds the Room database and ViewModel, sets the Compose content
├── PeopleViewModel.kt         # Directory state, add/delete, validation hand-off
├── model/Person.kt            # Role, sealed Person model, pure validation
├── data/PeopleDatabase.kt     # Room entity, DAO, database, repository
└── ui/PeopleApp.kt            # The single adaptive screen: form + list
```

## Build and test

Open the project in Android Studio and run the `app` configuration, or from a terminal:

```powershell
.\gradlew.bat testDebugUnitTest assembleDebug
```

The instrumented tests (including the Compose UI tests) need a device or emulator:

```powershell
.\gradlew.bat connectedDebugAndroidTest
```

## License

MIT — see [LICENSE](LICENSE).
