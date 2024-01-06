# Fediverse Redirect
A simple suite of apps for automatically launching fediverse links in your preferred Mastodon or Lemmy client.

## Use-Cases
Mastodon and Lemmy are both examples of federated social media. This is mostly a good thing, for a whole host of reasons, but it does have one notable disadvantage: deep linking support.

When you tap a Twitter link in your browser and your phone opens the Twitter app instead of the Twitter website to view the post, that's an example of deep linking. The trouble with federated social media is three-fold:
1. There are a lot of different instances at different addresses running the same or interoperable software.
2. Android only lets app developers declare supported deep link domains at compile time: users can't add custom domains, and domains can only be added through app updates. Many developers understandably don't want to maintain a list of thousands of domains.
3. Android really isn't set up for a single app to support more than a few different domains for deep links.

Fediverse Redirect aims to solve the first two issues and somewhat solve the third.

By using [Fediverse Observer](https://fediverse.observer) as a data source, Fediverse Redirect can maintain an up-to-date list of active domains.

Once you download the app, you can choose your client app and then enable all supported domains, so that when you tap a recognized link, it gets passed to the proper Fediverse Redirect variant.

The Fediverse Redirect variant then sends the link directly to the chosen client app for it to handle.

Since the point of Fediverse Redirect is only to change where supported links open, the maintenance of supported domains isn't extra busywork that has to be done in addition to other features and fixes; it's literally all the app does.

Client developers do need to do some work for Fediverse Redirect to support them, but it's a one-time thing, and instructions are available below.

## Screenshots

<img src="https://github.com/zacharee/MastodonRedirect/assets/9020352/839496b6-827f-4b90-a322-f6c33772354e" width="400"> <img src="https://github.com/zacharee/MastodonRedirect/assets/9020352/353cc22a-0bfc-4497-bcb0-6214be7a63c4" width="400"> <img src="https://github.com/zacharee/MastodonRedirect/assets/9020352/e3853f1c-082e-4e87-859b-7df95b5ebc8f" width="400"> <img src="https://github.com/zacharee/MastodonRedirect/assets/9020352/a5acb398-7f54-42e2-bb5a-561feed4ae08" width="400">

## Downloads
Fediverse Redirect has different variants depending on which social media network you want to use. Download the appropriate APK for your desired social network below.

### GitHub
[![GitHub Release](https://img.shields.io/github/v/release/zacharee/MastodonRedirect?style=for-the-badge&logo=github&label=Mastodon%20%2B%20Lemmy%20Redirect&color=orange)](https://github.com/zacharee/MastodonRedirect/releases)

### IzzyOnDroid
[![Mastodon Redirect](https://img.shields.io/endpoint?url=https%3A%2F%2Fapt.izzysoft.de%2Ffdroid%2Fapi%2Fv1%2Fshield%2Fdev.zwander.mastodonredirect&style=for-the-badge&logo=f-droid&label=Mastodon%20Redirect)](https://apt.izzysoft.de/fdroid/index/apk/dev.zwander.mastodonredirect/)
[![Lemmy Redirect](https://img.shields.io/endpoint?url=https%3A%2F%2Fapt.izzysoft.de%2Ffdroid%2Fapi%2Fv1%2Fshield%2Fdev.zwander.lemmyredirect&style=for-the-badge&logo=f-droid&label=Lemmy%20Redirect)](https://apt.izzysoft.de/fdroid/index/apk/dev.zwander.lemmyredirect/)

## Supported Domains
Currently, most domains on [Fediverse Observer](https://fediverse.observer) are supported.

Fediverse Redirect supports most active and alive instances, but excludes dead instances and instances that haven't had any activity recently. This is to keep the list as short as possible and avoid crashes.

Fediverse Redirect also supports the `web+activity+http`, `web+activity+https`, and `web+ap` URL schemes. The expectation is that the full post or profile URL will follow.

Examples:
```
// Post
web+ap://androiddev.social/@wander1236/110699242324667418

// Profile
web+ap://androiddev.social/@wander1236
```

[Fedi Links Project](https://fedilinks.org/).

## Setup
If any domains aren't enabled for handling by the Fediverse Redirect variant, the app will let you know and provide you buttons for enabling them.

Enabling each supported domain one at a time is possible, but tedious. Instead, Fediverse Redirect can use [Shizuku](https://shizuku.rikka.app) to automatically enable all links at once. The setup for Shizuku is a little complex, but can be done completely on-device on Android 11 and later. It is also only needed once for the initial setup or for enabling domains added in app updates.

Alternatively, you can use [LinkSheet](https://github.com/1fexd/LinkSheet) to have supported domains open in Fediverse Redirect. LinkSheet needs to be set as your default browser and then acts as a much more comprehensive and usable version of Android's built-in link handling options.

## Usage
Open the chosen Fediverse Redirect variant and select your preferred client.

## Client Support
Unfortunately, many Fediverse clients don't have a way for Fediverse Redirect to interface with them.

Fediverse Redirect relies on clients having a link sharing target that can parse and open fediverse links. 

Clients such as Tusky do have share targets, but they can only be used to create new posts, with the shared link as the content. Other clients have no share targets at all.

Mastodon Redirect currently supports the following clients:
- [Elk (PWA: Stable or Canary)](https://github.com/elk-zone/elk).
- [Fedilab (F-Droid or Play Store)](https://github.com/stom79/Fedilab).
- [Mastodon](https://github.com/mastodon/mastodon-android).
- [Megalodon](https://github.com/sk22/megalodon).
- [Moshidon (Stable or Nightly)](https://github.com/LucasGGamerM/moshidon).
- [Phanpy (PWA: Stable or Dev)](https://hachyderm.io/@phanpy).
- [Subway Tooter](https://github.com/tateisu/SubwayTooter).
- [Tooot](https://github.com/tooot-app/app).
- [Trunks (Native or Web)](https://mastodon.social/@trunksapp).

---------------------------

Lemmy Redirect currently supports the following clients:
- [Eternity](https://codeberg.org/Bazsalanszky/Eternity).
- [Jerboa](https://github.com/dessalines/jerboa).
- [Liftoff](https://github.com/liftoff-app/liftoff).
- [Summit](https://lemmy.world/c/summit).
- [Sync](https://github.com/laurencedawson/sync-for-lemmy).
- [Thunder](https://github.com/thunder-app/thunder).

And the following auto-discovery clients:
- [Raccoon](https://github.com/diegoberaldin/RaccoonForLemmy).

---------------------------

PeerTube Redirect currently supports the following clients:
- ~~[Fedilab (F-Droid or Play Store)](https://github.com/stom79/Fedilab)~~ (Currently disabled as link handling is broken).
- ~~[Grayjay (Stable, Unstable, or Play Store)](https://grayjay.app)~~ (Currently disabled as the PeerTube plugin only respects Grayjay's instance domain and also uses the wrong URL format to check for compatibility).
- [NewPipe (Release or Debug-Main)](https://github.com/TeamNewPipe/NewPipe).

If your favorite client isn't on the list, consider creating an issue on their code repository or issue tracker linking to the section below, ***but please search through the existing issues first, including ones that have been closed***. Pestering developers won't help anyone.

## Adding Client Support
If you're the developer of a Fediverse client and want to add support for Fediverse Redirect into your app, here's how.

### Automatic
You can let Fediverse Redirect automatically discover your app by filtering for a custom Intent and parsing the data as a URL.

#### Create a discoverable target.
In your `AndroidManifest.xml`, add the following intent filter inside the relevant Activity tag:

Mastodon Redirect:
```xml
<intent-filter>
    <action android:name="dev.zwander.mastodonredirect.intent.action.OPEN_FEDI_LINK" />
    
    <category android:name="android.intent.category.DEFAULT" />
</intent-filter>
```

Lemmy Redirect:
```xml
<intent-filter>
    <action android:name="dev.zwander.lemmyredirect.intent.action.OPEN_FEDI_LINK" />
    
    <category android:name="android.intent.category.DEFAULT" />
</intent-filter>
```

PeerTube Redirect:
```xml
<intent-filter>
    <action android:name="dev.zwander.peertuberedirect.intent.action.OPEN_FEDI_LINK" />
    
    <category android:name="android.intent.category.DEFAULT" />
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

Once you've implemented support, feel free to open an issue or PR to have it added to Fediverse Redirect.

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
In order to build Fediverse Redirect, you'll need two things:
1. The latest [Android Studio Canary](https://developer.android.com/studio/preview) build.  
2. ~~A [modified Android SDK](https://github.com/Reginer/aosp-android-jar) with hidden APIs exposed.~~

~~Download the modified SDK corresponding to Fediverse Redirect's current `compile.sdk` value (found in [gradle.properties](https://github.com/zacharee/MastodonRedirect/tree/main/gradle.properties)) and follow the instructions provided in the link above to install it.~~

## Contributing
If you want to add support for another app:

1. Find the `LaunchStrategy.kt` file for the relevant variant. For example, if you want to add support for an app to Lemmy Redirect, open `lemmyredirect/src/main/java/dev/zwander/lemmyredirect/util/LaunchStrategy.kt`.
2. Go to the `strings.xml` file for the variant and add the name of the app there.
3. Create a new data object extending the relevant `LaunchStrategyRootGroup` class (for Lemmy, it would be `LemmyLaunchStrategyRootGroup`). The argument the base class takes is a reference to the string resource you added.
4. Nested in that object, create another object extending the relevant `LaunchStrategy` class. This takes a key (unique to the app) and a variant label reference.
5. Override the `createIntents()` function and return a list of Intents to attempt to launch. Usually, only one Intent is needed.
6. Make sure to annotate both objects with `@Keep`.

Fediverse Redirect will automatically pick up the new class and show it as an option.

Check out the other objects in the file for examples.

## Error Reporting
Fediverse Redirect uses Bugsnag for error reporting.

<a href="https://www.bugsnag.com"><img src="https://assets-global.website-files.com/607f4f6df411bd01527dc7d5/63bc40cd9d502eda8ea74ce7_Bugsnag%20Full%20Color.svg" width="200"></a>
