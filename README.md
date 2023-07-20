# Mastodon Redirect
A simple app for automatically launching fediverse links in your preferred Mastodon client.

## Supported Domains
Currently, most domains on https://instances.social are supported.

Mastodon Redirect supports most active and alive instances, but excludes dead instances and instances that haven't had any activity recently. This is to keep the list as short as possible and avoid crashes.

Mastodon Redirect also (sort of) supports the `web+activity+http` and `web+activity+https` URL schemes. The expectation is that the full post or profile URL will follow.

Examples:
```
// Post
web+activity+https://androiddev.social/@wander1236/110699242324667418

// Profile
web+activity+https://androiddev.social/@wander1236
```

## Downloads
[![GitHub Release](https://img.shields.io/github/v/release/zacharee/MastodonRedirect?style=for-the-badge&logo=github&label=GitHub)](https://github.com/zacharee/MastodonRedirect/releases)
[![IzzyOnDroid](https://img.shields.io/endpoint?url=https%3A%2F%2Fapt.izzysoft.de%2Ffdroid%2Fapi%2Fv1%2Fshield%2Fdev.zwander.mastodonredirect&style=for-the-badge&logo=f-droid)](https://apt.izzysoft.de/fdroid/index/apk/dev.zwander.mastodonredirect/)

## Setup
If any domains aren\'t enabled for handling by Mastodon Redirect, the app will let you know and provide you buttons for enabling them.

Enabling each supported domain one at a time is possible, but tedious. Instead, Mastodon Redirect can use [Shizuku](https://shizuku.rikka.app) to automatically enable all links at once. The setup for Shizuku is a little complex, but can be done completely on-device on Android 11 and later. It is also only needed once for the initial setup or for enabling domains added in app updates.

## Usage
Open Mastodon Redirect and select your preferred client.

## Client Support
Unfortunately, many Mastodon clients don't have a way for Mastodon Redirect to interface with them.

Mastodon redirect relies on clients having a link sharing target that can parse and open fediverse links. 

Clients such as Tusky and Trunks do have share targets, but they can only be used to create new posts, with the shared link as the content. Other clients have no share targets at all.

Mastodon Redirect currently supports the following clients:
- [Fedilab (F-DROID or Play Store)](https://github.com/stom79/Fedilab).
- [Megalodon](https://github.com/sk22/megalodon).
- [Moshidon (Stable or Nightly)](https://github.com/LucasGGamerM/moshidon).
- [Subway Tooter](https://github.com/tateisu/SubwayTooter).
- [Elk (PWA)](https://github.com/elk-zone/elk).
- [Tooot](https://github.com/tooot-app/app).

## Adding Client Support
If you're the developer of a Mastodon client and want to add support for Mastodon Redirect into your app, here's how.

### Automatic
You can let Mastodon Redirect automatically discover your app by filtering for a custom Intent and parsing the data as a URL.

Note: right now, Mastodon Redirect doesn't support auto discovery, but it should be added soon.

#### Create a discoverable target.
In your `AndroidManifest.xml`, add the following intent filter inside the relevant Activity tag:

```xml
<intent-filter>
    <action android:name="dev.zwander.mastodonredirect.intent.action.OPEN_FEDI_LINK"/>
    <category android:name="android.intent.category.DEFAULT"/>
</intent-filter>
```

Inside the Activity itself:

```kotlin
override fun onCreate(savedInstanceState: Bundle?) {
    // ...

    val url = intent?.data?.toString()

    // Validate `url`.
    // Pass it to your internal link parser to find the post ID and such.
    // Open in your thread/profile viewer component.
}
```

### Manual
The high level process is pretty simple: expose some way for your app to be launched that accepts a URL and tries to parse it as a fediverse link to open as a post or profile. There are a few ways you can do this.

Once you've implemented support, feel free to open an issue or PR to have it added to Mastodon Redirect.

#### Create a share target.
Note: this will cause your app to appear in the share menu when a user chooses to share any text, not just links. If your app already has a share target for pasting the shared text into a new post draft, it might make sense to reuse that target with an option to open the shared link instead of only creating a new post.

Check out [Moshidon](https://github.com/LucasGGamerM/moshidon/blob/master/mastodon/src/main/java/org/joinmastodon/android/ExternalShareActivity.java) for an example.

In your `AndroidManifest.xml`, add the following intent filter inside the relevant Activity tag:

```xml
<intent-filter>
    <action android:name="android.intent.action.SEND"/>
    <category android:name="android.intent.category.DEFAULT"/>
    <data android:mimeType="text/*"/>
</intent-filter>
```

Inside the Activity itself:

```kotlin
override fun onCreate(savedInstanceState: Bundle?) {
    // ...
   
    if (intent?.action == Intent.ACTION_SHARE) {
       val sharedText = intent.getStringExtra(Intent.EXTRA_TEXT)
      
       // Validate that `sharedText` is a URL.
       // Pass it to your internal link parser to find the post ID and such.
       // Open in your thread/profile viewer component.
    }
}
```

#### Create a view target.
This is similar to the share target, but won't show up to users directly in the share menu.

In your `AndroidManifest.xml`, add the following intent filter inside the relevant Activity tag:

```xml
<intent-filter>
    <action android:name="android.intent.action.VIEW"/>
   <category android:name="android.intent.category.BROWSABLE"/>
    <category android:name="android.intent.category.DEFAULT"/>
    
    <data android:scheme="https" />
    <data android:scheme="http" />
    <data android:host="*" />
</intent-filter>
```

Inside the Activity itself:

```kotlin
override fun onCreate(savedInstanceState: Bundle?) {
   // ...
    
    val url = intent?.data?.toString()

    // Validate `url`.
    // Pass it to your internal link parser to find the post ID and such.
    // Open in your thread/profile viewer component.
}
```

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
