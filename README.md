# Mastodon Redirect
A simple app for automatically launching fediverse links in your preferred Mastodon client.

## Supported Domains
Currently, most domains on https://instances.social are supported.

Mastodon Redirect supports most active and alive instances, but excludes dead instances and instances that haven't had any activity recently. This is to keep the list as short as possible and avoid crashes.

## Downloads
[![GitHub Release](https://badgen.net/github/release/zacharee/MastodonRedirect/?icon=github&style=for-the-badge)](https://github.com/zacharee/MastodonRedirect/releases)
[![IzzyOnDroid](https://img.shields.io/endpoint?url=https%3A%2F%2Fapt.izzysoft.de%2Ffdroid%2Fapi%2Fv1%2Fshield%2Fdev.zwander.mastodonredirect&style=for-the-badge&logo=f-droid)](https://apt.izzysoft.de/fdroid/index/apk/dev.zwander.mastodonredirect/)

## Setup
If any domains aren\'t enabled for handling by Mastodon Redirect, the app will let you know and provide you buttons for enabling them.

Enabling each supported domain one at a time is possible, but tedious. Instead, Mastodon Redirect can use [Shizuku](https://shizuku.rikka.app) to automatically enable all links at once. The setup for Shizuku is a little complex, but can be done completely on-device on Android 11 and later. It is also only needed once for the initial setup or for enabling domains added in app updates.

## Usage
Open Mastodon Redirect and select your preferred client.

## Client Support
Unfortunately, most Mastodon clients don't have a way for Mastodon Redirect to interface with them.

Mastodon redirect relies on clients having a link sharing target that can parse and open fediverse links. 

Clients such as Tusky and Trunks do have share targets, but they can only be used to create new posts, with the shared link as the content. Other clients have no share targets at all.

Mastodon Redirect currently supports the following clients:
- [Fedilab (F-DROID or Play Store)](https://github.com/stom79/Fedilab).
- [Megalodon](https://github.com/sk22/megalodon).
- [Moshidon (Stable or Nightly)](https://github.com/LucasGGamerM/moshidon).
- [Subway Tooter](https://github.com/tateisu/SubwayTooter).

## Building
In order to build Mastodon Redirect, you'll need two things:
1. The latest [Android Studio Canary](https://developer.android.com/studio/preview) build.
2. A [modified Android SDK](https://github.com/Reginer/aosp-android-jar) with hidden APIs exposed.

Download the modified SDK corresponding to Mastodon Redirect's current `compileSdk` value (found in the module-level [build.gradle.kts](https://github.com/zacharee/MastodonRedirect/tree/main/app/build.gradle.kts)) and follow the instructions provided in the link above to install it.

## Contributing
If you want to add support for another app, here's the process:

1. Open `strings.xml` and create a new string for the client's name.
2. Open `LaunchStrategy.kt` and scroll to the bottom.
3. Create a new object extending `LaunchStrategy`:
    ```kotlin
    object YourNewClient : LaunchStrategy("UNIQUE_KEY_FOR_CLIENT", R.string.name_of_string_you_added) {
        override fun Context.createIntents(url: String?): List<Intent> {
            // Return a list of Intents for Mastodon Redirect to try launching.
            // Some apps, such as Fedilab, have different package names depending on
            // the install source, so may need multiple attempts to launch.
        }
    }
    ```
4. Add the new object to the `launchStrategies` in `LaunchStrategy.kt`:
   ```kotlin
   val launchStrategies by lazy { 
        mapOf(
            Megalodon.key to Megalodon,
            // ...
            YourNewClient.key to YourNewClient,
        ).toSortedMap()
   }
   ```

## Error Reporting
Mastodon Redirect uses Bugsnag for error reporting.

<a href="https://www.bugsnag.com"><img src="https://assets-global.website-files.com/607f4f6df411bd01527dc7d5/63bc40cd9d502eda8ea74ce7_Bugsnag%20Full%20Color.svg" width="200"></a>
