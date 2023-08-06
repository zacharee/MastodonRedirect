# Mastodon/Lemmy Redirect
A simple pair of apps for automatically launching fediverse links in your preferred Mastodon/Lemmy client.

## Use-Cases:
Mastodon and Lemmy are both examples of federated social media. This is mostly a good thing, for a whole host of reasons, but it does have one notable disadvantage: deep linking support.

When you tap a Twitter link in your browser and your phone opens the Twitter app instead of the Twitter website to view the post, that's an example of deep linking. The trouble with federated social media is three-fold:
1. There are a lot of different instances at different addresses running the same or interoperable software.
2. Android only lets app developers declare supported deep link domains at compile time: users can't add custom domains, and domains can only be added through app updates. Many developers understandably don't want to maintain a list of thousands of domains.
3. Android really isn't set up for a single app to support more than a few different domains for deep links.

Mastodon/Lemmy Redirect aims to solve the first two issues and somewhat solve the third.

By using the [instances.social](https://instances.social) and [Lemmy Explorer](https://lemmyverse.net/communities) APIs, Mastodon/Lemmy Redirect is able to maintain an up-to-date list of supported domains.

Once you download the app, you can choose your client app and then enable all supported domains, so that when you tap a recognized link, it gets passed to Mastodon/Lemmy Redirect.

Mastodon/Lemmy Redirect then sends the link directly to the chosen client app for it to handle.

Since the point of Mastodon/Lemmy Redirect is only to change where supported links open, the maintenance of supported domains isn't extra busywork that has to be done in addition to other features and fixes; it's literally all the app does.

Client developers do need to do some work for Mastodon/Lemmy Redirect to support them, but it's a one-time thing, and instructions are available below.

## Downloads
[![GitHub Release](https://img.shields.io/github/v/release/zacharee/MastodonRedirect?style=for-the-badge&logo=github&label=GitHub&color=orange)](https://github.com/zacharee/MastodonRedirect/releases)
[![IzzyOnDroid](https://img.shields.io/endpoint?url=https%3A%2F%2Fapt.izzysoft.de%2Ffdroid%2Fapi%2Fv1%2Fshield%2Fdev.zwander.mastodonredirect&style=for-the-badge&logo=f-droid&label=IzzyOnDroid%20-%20Mastodon%20Redirect)](https://apt.izzysoft.de/fdroid/index/apk/dev.zwander.mastodonredirect/)
[![IzzyOnDroid](https://img.shields.io/endpoint?url=https%3A%2F%2Fapt.izzysoft.de%2Ffdroid%2Fapi%2Fv1%2Fshield%2Fdev.zwander.lemmyredirect&style=for-the-badge&logo=f-droid&label=IzzyOnDroid%20-%20Lemmy%20Redirect)](https://apt.izzysoft.de/fdroid/index/apk/dev.zwander.lemmyredirect/)

## Supported Domains
Currently, most domains on https://instances.social and https://lemmyverse.net/communities are supported.

Mastodon/Lemmy Redirect supports most active and alive instances, but excludes dead instances and instances that haven't had any activity recently. This is to keep the list as short as possible and avoid crashes.

Mastodon/Lemmy Redirect also (sort of) supports the `web+activity+http` and `web+activity+https` URL schemes. The expectation is that the full post or profile URL will follow.

Examples:
```
// Post
web+activity+https://androiddev.social/@wander1236/110699242324667418

// Profile
web+activity+https://androiddev.social/@wander1236
```

## Setup
If any domains aren't enabled for handling by Mastodon/Lemmy Redirect, the app will let you know and provide you buttons for enabling them.

Enabling each supported domain one at a time is possible, but tedious. Instead, Mastodon/Lemmy Redirect can use [Shizuku](https://shizuku.rikka.app) to automatically enable all links at once. The setup for Shizuku is a little complex, but can be done completely on-device on Android 11 and later. It is also only needed once for the initial setup or for enabling domains added in app updates.

## Usage
Open Mastodon/Lemmy Redirect and select your preferred client.

## Client Support
Unfortunately, many Mastodon and Lemmy clients don't have a way for Mastodon/Lemmy Redirect to interface with them.

Mastodon/Lemmy Redirect relies on clients having a link sharing target that can parse and open fediverse links. 

Clients such as Tusky and Trunks do have share targets, but they can only be used to create new posts, with the shared link as the content. Other clients have no share targets at all.

Mastodon Redirect currently supports the following clients:
- [Elk (PWA: Stable or Canary)](https://github.com/elk-zone/elk).
- [Fedilab (F-Droid or Play Store)](https://github.com/stom79/Fedilab).
- [Mastodon](https://github.com/mastodon/mastodon-android).
- [Megalodon](https://github.com/sk22/megalodon).
- [Moshidon (Stable or Nightly)](https://github.com/LucasGGamerM/moshidon).
- [Subway Tooter](https://github.com/tateisu/SubwayTooter).
- [Tooot](https://github.com/tooot-app/app).

Lemmy Redirect currently supports the following clients:
- [Infinity](https://codeberg.org/Bazsalanszky/Infinity-For-Lemmy).
- [Jerboa](https://github.com/dessalines/jerboa).
- [Liftoff](https://github.com/liftoff-app/liftoff).
- [Summit](https://lemmy.world/c/summit).
- [Sync](https://github.com/laurencedawson/sync-for-lemmy).

If your favorite client isn't on the list, consider creating an issue on their code repository or issue tracker linking to the section below, ***but please search through the existing issues first, including ones that have been closed***. Pestering developers won't help anyone.

## Adding Client Support
If you're the developer of a Mastodon client and want to add support for Mastodon/Lemmy Redirect into your app, here's how.

### Automatic
You can let Mastodon/Lemmy Redirect automatically discover your app by filtering for a custom Intent and parsing the data as a URL.

Note: right now, Mastodon/Lemmy Redirect doesn't support auto discovery, but it should be added soon.

#### Create a discoverable target.
In your `AndroidManifest.xml`, add the following intent filter inside the relevant Activity tag:

Mastodon Redirect:
```xml
<intent-filter>
    <action android:name="dev.zwander.mastodonredirect.intent.action.OPEN_FEDI_LINK"/>
    <category android:name="android.intent.category.DEFAULT"/>
</intent-filter>
```

Lemmy Redirect:
```xml
<intent-filter>
    <action android:name="dev.zwander.lemmyredirect.intent.action.OPEN_FEDI_LINK"/>
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

Once you've implemented support, feel free to open an issue or PR to have it added to Mastodon/Lemmy Redirect.

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
In order to build Mastodon/Lemmy Redirect, you'll need two things:
1. The latest [Android Studio Canary](https://developer.android.com/studio/preview) build.
2. A [modified Android SDK](https://github.com/Reginer/aosp-android-jar) with hidden APIs exposed.

Download the modified SDK corresponding to Mastodon/Lemmy Redirect's current `compile.sdk` value (found in [gradle.properties](https://github.com/zacharee/MastodonRedirect/tree/main/gradle.properties)) and follow the instructions provided in the link above to install it.

## Contributing
If you want to add support for another app:

Until development slows down, check out the `LaunchStrategy.kt` file for how to add new apps.

## Error Reporting
Mastodon/Lemmy Redirect uses Bugsnag for error reporting.

<a href="https://www.bugsnag.com"><img src="https://assets-global.website-files.com/607f4f6df411bd01527dc7d5/63bc40cd9d502eda8ea74ce7_Bugsnag%20Full%20Color.svg" width="200"></a>
