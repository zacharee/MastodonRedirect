#!/usr/bin/env python3
"""Update every app's HTTP intent filter from FediDB.

The software names listed for each app below are used to decide which app a
server belongs to. Any software that is not listed by an app falls back to
`FALLBACK_APP`
"""

from __future__ import annotations

from enum import Enum

import argparse
import gzip
import json
import re
import sys
import textwrap
import urllib.request
from pathlib import Path


class App(Enum):
    LEMMY_REDIRECT = "lemmyredirect"
    MASTODON_REDIRECT = "mastodonredirect"
    PEERTUBE_REDIRECT = "peertuberedirect"

DATASET_URL = "https://api.fedidb.org/datasets/servers.json.gz"
FALLBACK_APP = App.MASTODON_REDIRECT

# Software handled by each app module. Anything not listed here goes to
# FALLBACK_APP.
APP_SOFTWARE: dict[App, list[str]] = {
    App.LEMMY_REDIRECT: [
        "lemmy",
        "kbin",
        "mbin",
        "piefed",
        "lotide",
        "repostea",
        "nodebb",
        "discourse",
    ],
    App.MASTODON_REDIRECT: [
        "mastodon",
        "calckey",
        "misskey",
        "pleroma",
        "friendica",
        "catodon",
        "akkoma",
        "gotosocial",
        "firefish",
        "diaspora",
        "hometown",
        "iceshrimp",
        "hubzilla",
        "nextcloud social",
        "pixelfed",
        "sharkey",
        "wildebeest",
        "bridgy-fed",
        "redmatrix",
        "socialhome",
        "writefreely",
        "plume",
        "mostr",
        "loops",
        "ghost",
        "foundkey",
        "birdsitelive",
        "activities-next",
        "activity-relay",
        "activitypods",
        "activitypub-bot",
        "activitypub-dot-bot",
        "activitypub-firebase",
        "activitypub-for-laravel",
        "aethon",
        "ai-social",
        "allheim",
        "anahita",
        "aniverse",
        "archival",
        "badgefed",
        "baudrate",
        "bikeodon",
        "bizzfed",
        "blogpublisher",
        "bonfire",
        "bookwyrm",
        "bovine",
        "bowrd",
        "broca",
        "bugle",
        "bulle",
        "calico",
        "careercupid",
        "cartopolis",
        "catteryx",
        "central-social",
        "cf-activitypub",
        "cfwdon-worker",
        "chara-social",
        "chat",
        "chatgpt-sites",
        "cheripia",
        "cherrypick",
        "cinderfish",
        "ciu",
        "clanspub",
        "cocoartz",
        "codenamemerp",
        "cofe",
        "coma",
        "commonpub",
        "community",
        "concrnt-ap-bridge",
        "contentnation",
        "cookifed",
        "cooltopia",
        "corpsekey",
        "dansal",
        "darak",
        "dawn-social",
        "demokratie-plattform",
        "disbigle",
        "dissipate",
        "dope-network",
        "dotsexy",
        "drupal",
        "ducktrip",
        "dutchbeercollective",
        "egregoros",
        "elgg",
        "elythia",
        "emissary",
        "enigma-bbs",
        "eunha",
        "exelula",
        "exodon",
        "fed",
        "fedibird",
        "fedibot",
        "fedidb",
        "fediforum",
        "fedihood",
        "fediloc",
        "fedipress",
        "fedired",
        "feditorrent",
        "fediverse-wrapped",
        "first",
        "fitpub",
        "flohmarkt",
        "fluff",
        "fobsocial",
        "forgejo",
        "frequency",
        "frogue",
        "funkwhale",
        "gancio",
        "gharab-tzereq",
        "gibrr",
        "glommer",
        "gnusocial",
        "goblog",
        "hackerspub",
        "handle",
        "harmony",
        "haruspex",
        "hatsu",
        "helios",
        "hickey",
        "hollo",
        "holos",
        "honk",
        "hotwaru",
        "iceshrimpnet",
        "incestoma",
        "incise",
        "infotograf",
        "inkwell",
        "irwin",
        "jolt",
        "juick",
        "karotter",
        "klonkt",
        "kmyblue",
        "kotori",
        "kpop-note",
        "kromosynth",
        "ktistec",
        "kuddes",
        "kult",
        "lastgnomelocation",
        "letter",
        "lightap",
        "litopia",
        "littlefedi",
        "liveloop",
        "mammuthus",
        "manyfold",
        "marge",
        "marten",
        "mast-go",
        "mastopocket",
        "matrix-appservice-activitypub",
        "matters-gateway-core",
        "mdverse",
        "meisskey",
        "menuverse-shopmenu",
        "mffgo",
        "microdotblog",
        "militant",
        "millbody",
        "misskey-rs",
        "mitra",
        "miuno",
        "mixednutz",
        "mixpal",
        "mobilizon",
        "mochi",
        "moim",
        "moimcookr",
        "moments",
        "moviebuff",
        "musorbox",
        "my",
        "my-mcr",
        "neodb",
        "neodb-skybridge",
        "neogroup",
        "nexkey",
        "noblogs",
        "nori",
        "normals",
        "nox",
        "nyap",
        "occookr",
        "oeee-cafe",
        "ohagi",
        "omicron",
        "openbook",
        "openspace",
        "paon",
        "pawkey",
        "peblish",
        "peermesh-pm-module-social",
        "plamenu",
        "plattform-activitypub",
        "podcast-peformance",
        "podcast-pizza",
        "polyculelive",
        "ponos",
        "posterchan",
        "pr0klon",
        "psephos",
        "pulsar",
        "quolibet",
        "reisetagebuch",
        "rooted-news",
        "rox",
        "rs-cms",
        "sabach",
        "samklang",
        "scenes-cloud",
        "scrapblog",
        "seiran",
        "selective-relay",
        "shelfy",
        "shoot",
        "shorkey",
        "sigilverse",
        "signalflow",
        "siliconbeest",
        "single-file-activitypub-server-in-php",
        "skept",
        "smithereen",
        "snac",
        "snna774net",
        "sociables",
        "social-wud",
        "soshy",
        "spica",
        "squidcity",
        "stabbypossum",
        "starling",
        "stegodon",
        "stella",
        "stronghold",
        "sukhi-fedi",
        "sxy",
        "takahe",
        "thoughts",
        "tinyap",
        "transit-fedilerts",
        "trough",
        "truefans",
        "typent",
        "uanna",
        "unet",
        "utopolis",
        "uwuzu",
        "vaak",
        "vernissage",
        "vulasocial",
        "vulasocial-staging",
        "vutuv",
        "wafrn",
        "weft",
        "weird",
        "welley",
        "wordpress",
        "writ",
        "wrok",
        "wxwclub",
        "xbuddy",
        "yoiyami",
        "yojo-art",
        "yonder",
        "yplev",
        "z-sns",
        "zensocial",
        "zerocat",
        "zsaso",
    ],
    App.PEERTUBE_REDIRECT: [
        "peertube",
        "cliptube",
        "clipsymphonyactivitypubservice",
        "openstreamingplatform",
        "bikoosh-watch",
    ],
}

REPO_ROOT = Path(__file__).resolve().parents[1]
HTTP_FILTER_RE = re.compile(
    r'(<intent-filter android:label="@string/http_filter">)(.*?)(</intent-filter>)',
    re.DOTALL,
)
HOST_LINE_RE = re.compile(r'^\s*<data android:host="[^"]*"\s*/>\s*$')
# Abort if an app would lose more than this fraction of its existing hosts.
MAX_HOST_DROP = 0.5
MIN_HOSTS_FOR_DROP_CHECK = 100
INVALID_HOST_CHARS_RE = re.compile(r'[\s"\'<>&/\\*]')


def normalize_software(name: str) -> str:
    return re.sub(r"[\s_]+", "-", name.strip().lower())


def download_servers() -> list[dict]:
    request = urllib.request.Request(
        DATASET_URL,
        headers={"User-Agent": "MastodonRedirect-http-filter-updater"},
    )

    with urllib.request.urlopen(request, timeout=60) as response:
        payload = response.read()
        content_encoding = response.headers.get("Content-Encoding")

    if content_encoding == "gzip" or payload[:2] == b"\x1f\x8b":
        payload = gzip.decompress(payload)

    servers = json.loads(payload)
    if not isinstance(servers, list):
        raise SystemExit("Unexpected dataset format: expected a list of servers")

    return servers


def valid_host(domain: object) -> str | None:
    if not isinstance(domain, str):
        return None

    host = domain.strip().lower()

    # Filter out blank, relative, and malformed hosts.
    if not host or host.startswith(".") or "." not in host:
        return None
    if INVALID_HOST_CHARS_RE.search(host):
        return None

    return host


def count_existing_hosts(path: Path) -> int:
    text = path.read_text(encoding="utf-8")
    match = HTTP_FILTER_RE.search(text)
    if not match:
        return 0
    return sum(1 for line in match.group(2).split("\n") if HOST_LINE_RE.match(line))


def update_manifest(path: Path, hosts: list[str], dry_run: bool) -> bool:
    text = path.read_text(encoding="utf-8")
    match = HTTP_FILTER_RE.search(text)

    if not match:
        raise SystemExit(f'No http_filter intent-filter found in {path}')

    block = match.group(2)
    lines = block.split("\n")
    kept: list[str] = []

    for line in lines:
        if HOST_LINE_RE.match(line):
            continue
        kept.append(line)

    scheme_index = max(
        (index for index, line in enumerate(kept) if 'android:scheme="' in line),
        default=None,
    )
    if scheme_index is None:
        raise SystemExit(f"No scheme entries found in the http_filter in {path}")

    insert_at = scheme_index + 1
    if insert_at < len(kept) and not kept[insert_at].strip():
        insert_at += 1

    host_lines = [f'                <data android:host="{host}" />' for host in hosts]
    new_block = "\n".join(kept[:insert_at] + host_lines + kept[insert_at:])
    new_text = text[: match.start(2)] + new_block + text[match.end(2) :]

    if new_text == text:
        return False
    if not dry_run:
        path.write_text(new_text, encoding="utf-8")
    return True


def main() -> int:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument(
        "--dry-run",
        action="store_true",
        help="Report what would change without writing any files",
    )
    parser.add_argument(
        "--force",
        action="store_true",
        help="Skip the safety check that aborts when an app loses most of its hosts",
    )
    args = parser.parse_args()

    missing_manifests = [
        app.value
        for app in APP_SOFTWARE
        if not (REPO_ROOT / app.value / "src/main/AndroidManifest.xml").is_file()
    ]
    if missing_manifests:
        raise SystemExit(f"Missing AndroidManifest.xml for: {', '.join(missing_manifests)}")

    servers = download_servers()
    if not servers:
        raise SystemExit("The dataset is empty; refusing to update")

    software_to_app: dict[str, App] = {}
    for app, software_names in APP_SOFTWARE.items():
        for name in software_names:
            key = normalize_software(name)
            if key in software_to_app:
                raise SystemExit(
                    f'Software "{key}" is listed by both {software_to_app[key].value} and {app.value}'
                )
            software_to_app[key] = app

    hosts_by_app: dict[App, set[str]] = {app: set() for app in APP_SOFTWARE}
    unmatched_software: set[str] = set()
    ignored_hosts = 0

    for server in servers:
        host = valid_host(server.get("domain"))
        if host is None:
            ignored_hosts += 1
            continue

        software = server.get("software")
        software_key = normalize_software(software) if isinstance(software, str) else ""

        app = software_to_app.get(software_key)
        if app is None:
            app = FALLBACK_APP
            if software_key:
                unmatched_software.add(software_key)
            else:
                unmatched_software.add("<BLANK>")

        hosts_by_app[app].add(host)

    if not args.force:
        for app in APP_SOFTWARE:
            manifest = REPO_ROOT / app.value / "src/main/AndroidManifest.xml"
            existing = count_existing_hosts(manifest)
            new = len(hosts_by_app[app])
            if existing >= MIN_HOSTS_FOR_DROP_CHECK and new < existing * (1 - MAX_HOST_DROP):
                raise SystemExit(
                    f"{app.value}: host count would drop from {existing} to {new}; "
                    "refusing to update (use --force to override)"
                )

    changed = []
    for app in sorted(APP_SOFTWARE, key=lambda app: app.value):
        manifest = REPO_ROOT / app.value / "src/main/AndroidManifest.xml"
        hosts = sorted(hosts_by_app[app])

        if update_manifest(manifest, hosts, args.dry_run):
            changed.append(app.value)

        print(f"{app.value}: {len(hosts)} domains")

    if ignored_hosts:
        print(f"Ignored {ignored_hosts} invalid domains")

    if unmatched_software:
        listed = ", ".join(sorted(unmatched_software))
        print(
            textwrap.fill(
                f"Software not listed by any app (sent to {FALLBACK_APP.value}): {listed}",
                width=100,
            )
        )

    if args.dry_run:
        print("Dry run: no files written")
    elif changed:
        print(f"Updated: {', '.join(changed)}")
    else:
        print("No changes")

    return 0


if __name__ == "__main__":
    sys.exit(main())
