# Mastodon Redirect
A simple app for automatically launching fediverse links in your preferred Mastodon client.

## Supported Domains
Currently, the top 150 or so Mastodon domains are supported. If the app is missing a domain you want, open an issue or PR to add it.

## Downloads
Check out the [Releases page](https://github.com/zacharee/MastodonRedirect/releases).

## Setup
Initial setup is a little tedious. Android doesn't really support the notion of an app handling hundreds of domains, which means you need to manually enable all of them.

Mastodon Redirect will let you know when any aren't enabled and direct you to the Android Settings page where you can enable them.

Unless you factory reset, you should only need to enable domains on first launch and when any new ones are added in updates.

## Usage
Open Mastodon Redirect and select your preferred client.

## Client Support
Unfortunately, most Mastodon clients don't have a way for Mastodon Redirect to interface with them.

Mastodon redirect relies on clients having a link sharing target that can parse and open fediverse links. 

Clients such as Tusky and Trunks do have share targets, but they can only be used to create new posts, with the shared link as the content. Other clients have no share targets at all.

## Contributing
If you want to add support for another app, here's the process:

1. Open `strings.xml` and create a new string for the client's name.
2. Open `LaunchStrategy.kt` and scroll to the bottom.
3. Create a new object extending `LaunchStrategy`:

    ```
    kotlin
    object YourNewClient : LaunchStrategy("UNIQUE_KEY_FOR_CLIENT", R.string.name_of_string_you_added) {
        override fun Context.createIntents(url: String?): List<Intent> {
            // Return a list of Intents for Mastodon Redirect to try launching.
            // Some apps, such as Fedilab, have different package names depending on
            // the install source, so may need multiple attempts to launch.
        }
    }
    ```
