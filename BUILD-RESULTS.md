# TV Dimmer 1.6.2 validation

7 November 2026

The only production behavior change is one slider key listener: Right events are consumed when the slider is already at its maximum. No layout, dimming, schedule, permission, default or other navigation logic was changed. The package version was incremented for installation as an update.

Completed checks:
- Compiled with JDK 17 against Android SDK Platform 35, packaged with AAPT2/D8 and signed with the existing update key.
- APK v2/v3 signatures verified.
- Installed as an update in the Android 8 / API 26 emulator.
- Native remote-key instrumentation reached brightness 10 and checked 20 additional Right presses plus 10 repeated/held Right key-down events. Focus remained on the slider and the value stayed at 10 throughout.
- Left still reduced the value; Right returned it to 10; Down still moved focus normally.
- No AndroidRuntime errors in the captured log.

Earlier layout, focus, animation, OFF/ON slider and Gradle wrapper checks apply to unchanged code and were not unnecessarily repeated. Full Android Gradle/lint execution was not repeated here; the included GitHub workflow performs those checks. Physical Xiaomi hardware was not retested here.
