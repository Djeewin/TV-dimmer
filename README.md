# TV Dimmer

Native Android TV project for Android 8.0 and later, with no third party runtime libraries and no internet permission. Designed for remote control and personal sideloading on a Xiaomi Mi TV Stick 4K. This is source code, not a compiled APK. It has not been compiled or tested on an emulator or physical TV in the creation environment. APK size and device compatibility remain unmeasured.

## What it does

A black, nonfocusable overlay dims other applications. The slider has ten steps. Level 1 is 95% black, leaving about 5% of the rendered picture; level 10 removes the overlay. The lowest comfortable level depends on the TV. This does not control physical backlight or exactly reproduce Android's privileged Extra Dim feature. The remote continues controlling the underlying app.

Daily selects all seven days. Weekly selects one day, initially today; change the checkboxes to your preferred day. Custom means any combination of weekdays. This version supports one recurring time window, not multiple independent rules or individual calendar dates. Start and end follow the TV's local time zone. An end earlier than or equal to the start is on the following day. Each selected start day owns its overnight end. With no end, the schedule stays dimmed after its first start until manually restored.

Apply settings commits the form. The slider saves its own value immediately and updates the currently saved dimming mode. If scheduling is enabled it controls whether dimming is active; otherwise the manual switch controls it. Restore Normal Now disables scheduling and manual dimming and resets level to 10. Boot startup restores the saved mode, including whether the current time is inside a scheduled window. It does not force dimming outside the window. The boot switch controls boot restoration; future enabled alarms still operate.

## Build an APK locally

1. Install Android Studio from https://developer.android.com/studio on your computer.
2. Extract this ZIP. Open the `tv-dimmer` folder as the Android Studio project.
3. Use JDK 17, Android SDK Platform 35, and Gradle 8.9. This project has no bundled Gradle wrapper binary. If Studio asks for a Gradle installation, install Gradle 8.9 from https://gradle.org/releases/ and select its local installation, or run `gradle wrapper --gradle-version 8.9` in this folder first.
4. Let Gradle sync download Android build tools. From a terminal in the project folder run:

   gradle :app:assembleDebug :app:lintDebug

5. The installable, automatically debug signed APK is `app/build/outputs/apk/debug/app-debug.apk`. Debug signing is suitable for personal testing. Keep the same signing key for future updates. For a release use Android Studio's Generate Signed App Bundle or APK and keep your private signing key safe.

Alternative without a local Android SDK: put the CONTENTS of `tv-dimmer` at the root of your own GitHub repository, including `.github`. In Actions run “Build debug APK”, then download the `tv-dimmer-debug-apk` artifact and extract the APK. This workflow has been supplied but not executed here. Hosted Actions availability and usage limits depend on your account. No repository has been created or code uploaded for you.

## Test before installing on the TV stick

You cannot execute an APK without installing it somewhere. Install it into an Android TV virtual device on your computer instead of your stick.

1. In Android Studio, open Device Manager, create a TV virtual device and download an Android TV system image. An Android 11 image is a useful baseline; also test a system image matching the Android version shown in the stick's About screen.
2. Start the emulator. Drag the built APK into it, or use `adb install -r app/build/outputs/apk/debug/app-debug.apk`.
3. Launch TV Dimmer from the TV app launcher. Grant “Display over other apps” using its first button. If the emulator's TV settings provide no permission screen, use:

   adb shell appops set com.example.tvdimmer SYSTEM_ALERT_WINDOW allow

4. On Android 12 or later allow “Alarms & reminders”. If the TV settings do not expose it, in your own emulator use:

   adb shell appops set com.example.tvdimmer SCHEDULE_EXACT_ALARM allow

5. Set level 6, turn on “Dim now”, leave schedule off, and Apply. Return Home and check other apps dim. Use the remote's arrow keys and center button throughout. Test levels 1 and 10 and Restore Normal Now.
6. Set a daily start two or three minutes ahead, with an end two minutes later. Enable schedule and Apply. Leave the app and verify both transitions. Repeat with a nonselected weekday and a custom weekday selection. Check an overnight interval as well.
7. With manual dimming active and boot startup enabled, run `adb reboot`, wait for the TV launcher, and verify restoration. With a scheduled window, verify boot both inside and outside the window. Also test changing the time zone and restarting the app.
8. On Android 12 and later, revoke exact alarm access and verify the app asks for it before enabling a schedule. Reopen and reapply after granting access again.
9. Test Home, Back, the keyboard and settings, video playback and app switching. Some system windows and apps that hide overlays will remain bright. A mouse or touchscreen may have input blocked under a very dark overlay on newer Android versions; this app is designed for a TV remote.
10. Remove the emulator installation when finished: `adb uninstall com.example.tvdimmer`.

The emulator verifies the UI and scheduling logic but cannot establish how Xiaomi firmware, HDMI output, HDR or protected video behaves. Those require testing on the actual stick.

## Install on the stick after emulator testing

Enable Developer options and the available debugging option in the stick's settings. Use Android SDK Platform Tools to connect to your own device using the pairing/connection method its firmware exposes. Classic TV network debugging commonly uses `adb connect TV_IP:5555`; newer wireless debugging may require `adb pair` first. Accept the on-screen authorization. With only the intended device connected:

    adb install -r app-debug.apk

Open TV Dimmer once and grant overlay permission. If the TV permission screen is unavailable, the same overlay appops command above can be run against the connected stick. Grant precise alarm access on Android 12 or later. Apply settings, test on Home first, then in each video app you use. Boot broadcasts require the app to have been launched at least once. Force stopping an app suppresses its automatic work until it is opened again. Vendor power management can also prevent automatic startup; it is not guaranteed on untested firmware. No root or accessibility service is required.

If the screen is too dark, reopen TV Dimmer and choose Restore Normal Now. From an authorized computer you can also immediately stop it with:

    adb shell am force-stop com.example.tvdimmer

Then reopen it and restore normal brightness. Uninstalling removes the overlay too.

## Implementation and limitations

A foreground service keeps a single overlay and an ongoing notification with a Restore action. Alarms use user granted precise scheduling; there is no repeating poll loop. Receivers restore alarms after boot, package replacement, clock and time zone changes. The service uses Android's specialUse foreground service type for persistent user requested dimming. The source targets API 35. Android and vendor rules can affect background starts, particularly after permission changes. Inspect the in-app status if automatic start is blocked, then open the app and Apply.

No analytics, advertising, accounts or network permission. Distribution through Play would require additional policy review, signing and current target API checks. This project is intended for personal sideloading.
