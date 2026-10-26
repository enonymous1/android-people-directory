# Changelog

> Public export note: this repository is a squashed single-commit export of the 2.0.0 modernization below. The original private history is not included.

## 2.0.0 — 2026-10-26

Modernization of the original CE05 assignment, on top of its original history.

- Rewritten in Kotlin with Jetpack Compose Material 3; the XML layouts and fragments are gone.
- One adaptive screen: form and list side by side on wide screens, stacked on narrow ones. The original only worked in landscape; portrait was a "please rotate" message.
- Data is persisted with Room. The original kept people in a public static list that was reset in `onCreate()`, so rotation or process death emptied the directory.
- The model is fixed: first and last names live once on the base type, and each role's unique field (student ID, teacher course, administrator program) is readable and shown in the list. The original subclasses shadowed the names and exposed no getters, so the unique fields were entered and then invisible.
- The list updates live when a person is added. The original required a manual Refresh, which also reset the spinner and snapped the form back to Student.
- Validation is real: blank and whitespace-only fields are rejected with inline errors, and input is trimmed. Adding a person clears the form and confirms; switching roles keeps each role's unsaved draft.
- People can be deleted from the list. (New in 2.0.0; v1.0 had no delete, edit, or detail view.)
- Toolchain: AGP 8.13.0, Gradle 8.14.3, Kotlin 2.2.0, Java 17, compile/target SDK 36, min SDK 24, Kotlin DSL with a version catalog, R8 for release builds.
- Package and application ID changed from `com.example.lyonsjustin_ce05` to `com.enonymous1.peopledirectory`; the app is now called People Directory.
- Added GitHub Actions CI (unit tests + debug build), an MIT LICENSE, and this changelog.
- Removed from tracking: the committed 4.2 MB release APK and the IDE configuration files. They were build output and editor state, not source.
- Tests: 7 validation unit tests, 3 ViewModel tests with a fake repository, and 4 instrumented tests (package name, empty state, add flow, validation errors — the last three are Compose UI tests; they need a device or emulator and are not run in CI).

## 1.0.0

Original CE05 assignment: Java, AppCompat, XML layouts and fragments, landscape-only people directory with Student, Teacher, and Administrator forms.
