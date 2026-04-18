#!/usr/bin/env python3
"""
Import games from the hardcoded JSON data into medialogger.db.
Fetches cover art via RAWG.io API, then produces a new backup zip.

Requirements:
    pip install requests Pillow

Usage:
    python import_games.py [source_backup.zip] [output_backup.zip]

Environment:
    RAWG_API_KEY  — free key at https://rawg.io/apiv2
"""

import os
import re
import sys
import uuid
import time
import shutil
import zipfile
import sqlite3
import requests
from PIL import Image
from io import BytesIO
from concurrent.futures import ThreadPoolExecutor, as_completed
from datetime import datetime, timezone

# ---------------------------------------------------------------------------
# Config
# ---------------------------------------------------------------------------

SCRIPT_DIR = os.path.dirname(os.path.abspath(__file__))
LOCAL_COVERS_DIR = os.path.join(SCRIPT_DIR, "media_covers")
ANDROID_COVERS_PREFIX = "/data/user/0/com.tristinbaker.lifeos/files/media_covers"
MAX_WIDTH = 400
JPEG_QUALITY = 70
RAWG_API_KEY = os.environ.get("RAWG_API_KEY", "")

# ---------------------------------------------------------------------------
# Games data
# ---------------------------------------------------------------------------

GAMES_BY_YEAR = {
    2021: [
        "Final Fantasy X","It Takes Two","Sekiro","Dark Souls 2","Dark Souls 3",
        "Bloodborne","Infamous Second Son","Mario 3D World","Super Mario World",
        "MLB The Show 21","Celeste","What Remains of Edith Finch","Gorogoa",
        "Donut County","Trails from Zero","Mass Effect","Silent Hill",
        "SpongeBob SquarePants The Movie Game","The Hobbit (GameCube)",
        "Rondo of Blood","Tak and the Power of Juju","Ys III (SNES)",
        "Final Fantasy X (again)","Cosmic Star Heroine","Cthulhu Saves Christmas",
        "New Super Mario Bros. 2 (100%)","Barry the Bunny (100%)",
        "Trover Saves the Universe","Grandia","Metal Gear Solid","Dragon Quest",
        "Panzer Paladin","To The Moon","Old School Musical","Super Mario 64",
        "The End is Nigh","Link's Awakening","Shovel Knight","Deltarune Chapter 1",
        "Blind Postman (100%)","Dagon","Portal Reloaded",
        "Spooky's Jump Scare Mansion","Pasadelo","Ittle Dew 2+","Heavensward",
        "Super Mario 64","Stormblood","Shadowbringers","Endwalker",
    ],
    2022: [
        "Pokemon Sigma Platinum","Mickey's Castle of Illusion",
        "DuckTales Remastered","Five Nights at Freddy's","Metal Gear Solid 2",
        "Dishonored","Bloodborne PSX","Resident Evil 5",
        "Kirby Nightmare in Dream Land","Descenders","Super Mario Land",
        "Kirby's Dream Land","Kirby's Dream Land 2","Elden Ring","Distance",
        "Dark Souls 3","Skate 3","Mario 64","Kingdom Hearts",
        "Pokémon HeartGold","Sir Lovelot (Platinum)","Rollin' (100%)",
        "Pokemon Emerald","Pokémon Ruby","Pokemon Ultra Sun (Randomized Nuzlocke)",
        "Kirby 64: The Crystal Shards","Grand Theft Auto V","Castlevania","Mafia",
        "Metal Gear Solid 3: Snake Eater","The Looker","Sonic Adventure 2",
        "Halo 3","Gitaroo Man","Silent Hill 2","Silent Hill 3","Silent Hill 4",
        "Silent Hill Origins","Mad Maestro","Sayonara Wild Hearts",
        "Dark Pictures Anthology: Little Hope","Faith","P.T.","Anatomy",
        "Turnip Boy Commits Tax Evasion","Pokemon Violet","Pokemon Black",
        "The Legend of Zelda: Ocarina of Time","Metal: Hellsinger",
    ],
    2023: [
        "Pokemon Crystal","A Dance of Fire and Ice","Dark Souls: Remastered",
        "Hitman","Final Fantasy",
        "The Legend of Heroes: Trails of Cold Steel III","Final Fantasy (100%)",
        "Bloodborne","Pokémon Snap","Pokémon Yellow",
        "Super Mario World: Return to Dragon Land","Super Mario Maker 2",
        "Super Mario Bros.","Yoshi's Story","Pokémon Stadium","Super Mario 64",
        "Celeste","Donkey Kong Country","Super Mario World","Learn2Kaizo",
        "Lies of P","Super Mario Bros. Wonder (100%)",
        "Dark Souls III (Broken Straight Sword only)",
        "Dark Souls II: Scholar of the First Sin","Dark Souls Remastered",
        "Dark Souls III","Elden Ring",
    ],
    2024: [
        "Kingdom Hearts: Chain of Memories","Kururin Paradise","Super Mario World",
        "Pocket Meat","Super Monkey Ball Jr.","Corn Kidz 64","Toree 3D","Toree 2",
        "Tony Hawk's Pro Skater 1+2 (100%)","Sonic the Hedgehog",
        "Streets of Rage","RoboCop: Rogue City","Minecraft",
    ],
    2025: [
        "Descenders (Platinum)","Scooby Doo and the Cyber Chase","Distance",
        "Portal","Scooby Doo (Commodore 64)","Pokemon Alpha Sapphire",
        "Romancing SaGa 2: Revenge of the Seven Heroes","Buckshot Roulette",
        "Postal Redux","The Walking Dead Season 1","The Walking Dead Season 2",
        "Tony Hawk's Pro Skater 3 (Remake) (100%)",
        "Tony Hawk's Pro Skater 4 (Remake) (100%)","s.p.l.i.t. (100%)",
        "Celeste (Classic)","Baldur's Gate III",
        "The Elder Scrolls IV: Oblivion (Remastered)",
        "Call of Duty: Black Ops","Silent Hill f","Clover Pit",
        "Indigo Prophecy","The Wolf Among Us",
    ],
    2026: [
        "Pokémon Legends: Z-A","Super Mario 64 (16 Star)",
        "Kingdom Hearts 0.2: Birth By Sleep - A Fragmentary Passage",
        "IdlescApe","Mafia: The Old Country",
    ],
}

# ---------------------------------------------------------------------------
# Helpers
# ---------------------------------------------------------------------------

def parse_flags(raw_title: str) -> tuple[str, bool, bool]:
    """Return (clean_title, has_platinum, has_100_percent)."""
    has_platinum = bool(re.search(r'\(Platinum\)', raw_title, re.IGNORECASE))
    has_100 = bool(re.search(r'\(100%\)', raw_title))
    title = raw_title
    title = re.sub(r'\s*\(Platinum\)', '', title, flags=re.IGNORECASE).strip()
    title = re.sub(r'\s*\(100%\)', '', title).strip()
    return title, has_platinum, has_100


def search_title_for_api(clean_title: str) -> str:
    """Strip all parentheticals for a cleaner RAWG search query."""
    return re.sub(r'\s*\([^)]*\)', '', clean_title).strip()


def year_to_timestamp_ms(year: int) -> int:
    """Return Unix ms for Dec 31 of the given year at noon UTC."""
    dt = datetime(year, 12, 31, 12, 0, 0, tzinfo=timezone.utc)
    return int(dt.timestamp() * 1000)


# ---------------------------------------------------------------------------
# RAWG cover fetching
# ---------------------------------------------------------------------------

def fetch_cover_url_rawg(title: str, api_key: str) -> str | None:
    query = search_title_for_api(title)
    if not query:
        return None
    try:
        resp = requests.get(
            "https://api.rawg.io/api/games",
            params={"key": api_key, "search": query, "page_size": 3, "search_exact": False},
            headers={"User-Agent": "LifeOS/1.0"},
            timeout=15,
        )
        if resp.status_code == 200:
            results = resp.json().get("results", [])
            for r in results:
                if r.get("background_image"):
                    return r["background_image"]
    except Exception as e:
        print(f"    RAWG error for '{query}': {e}")
    return None


def build_cover_url_map(unique_titles: list[str], api_key: str, max_workers: int = 8) -> dict[str, str]:
    """Return {clean_title: cover_url} for all titles, in parallel."""
    print(f"Fetching cover URLs for {len(unique_titles)} unique titles from RAWG...")
    results: dict[str, str] = {}
    completed = 0

    with ThreadPoolExecutor(max_workers=max_workers) as ex:
        future_to_title = {ex.submit(fetch_cover_url_rawg, t, api_key): t for t in unique_titles}
        for future in as_completed(future_to_title):
            title = future_to_title[future]
            url = future.result()
            if url:
                results[title] = url
            completed += 1
            if completed % 20 == 0:
                print(f"  Fetched {completed}/{len(unique_titles)} cover URLs...")

    found = sum(1 for v in results.values() if v)
    print(f"  Cover URLs found: {found}/{len(unique_titles)}")
    return results


# ---------------------------------------------------------------------------
# Image downloading
# ---------------------------------------------------------------------------

def download_and_resize(url: str, dest_path: str) -> str | None:
    try:
        resp = requests.get(
            url,
            headers={"User-Agent": "Mozilla/5.0"},
            timeout=30,
        )
        if resp.status_code != 200:
            return None
        img = Image.open(BytesIO(resp.content))
        if img.mode in ("RGBA", "P"):
            img = img.convert("RGB")
        if img.width > MAX_WIDTH:
            new_h = int(MAX_WIDTH / img.width * img.height)
            img = img.resize((MAX_WIDTH, new_h), Image.LANCZOS)
        img.save(dest_path, "JPEG", quality=JPEG_QUALITY, optimize=True)
        return dest_path
    except Exception as e:
        print(f"    Download error for {url}: {e}")
        return None


def download_covers(cover_url_map: dict[str, str], max_workers: int = 10) -> dict[str, str]:
    """Download images, return {clean_title: local_basename}."""
    os.makedirs(LOCAL_COVERS_DIR, exist_ok=True)
    print(f"Downloading {len(cover_url_map)} cover images...")
    local_map: dict[str, str] = {}
    completed = 0

    with ThreadPoolExecutor(max_workers=max_workers) as ex:
        futures = {}
        for title, url in cover_url_map.items():
            dest = os.path.join(LOCAL_COVERS_DIR, f"{uuid.uuid4()}.jpg")
            futures[ex.submit(download_and_resize, url, dest)] = (title, dest)

        for future in as_completed(futures):
            title, dest = futures[future]
            result = future.result()
            if result:
                local_map[title] = os.path.basename(result)
            completed += 1
            if completed % 20 == 0:
                print(f"  Downloaded {completed}/{len(cover_url_map)} images...")

    print(f"  Images saved: {len(local_map)}/{len(cover_url_map)}")
    return local_map


# ---------------------------------------------------------------------------
# Database
# ---------------------------------------------------------------------------

def patch_db(db_path: str, local_cover_map: dict[str, str], cover_url_map: dict[str, str]):
    conn = sqlite3.connect(db_path)
    cur = conn.cursor()

    cur.execute("DELETE FROM media_items WHERE type = 'GAME'")
    print(f"Deleted {cur.rowcount} existing GAME entries")

    created_at = int(datetime.now(tz=timezone.utc).timestamp() * 1000)
    inserted = 0
    missing_cover = 0

    for year, raw_titles in GAMES_BY_YEAR.items():
        date_completed = year_to_timestamp_ms(year)
        for raw_title in raw_titles:
            clean_title, has_platinum, has_100 = parse_flags(raw_title)
            cover_url = cover_url_map.get(clean_title)
            local_basename = local_cover_map.get(clean_title)
            cover_local = (
                f"{ANDROID_COVERS_PREFIX}/{local_basename}" if local_basename else None
            )
            if not cover_url:
                missing_cover += 1

            cur.execute(
                """
                INSERT INTO media_items (
                    title, coverUrl, coverLocalPath, rating, dateCompleted,
                    notes, type, platform, author, createdAt,
                    isRewatch, hasPlatinum, has100Percent
                ) VALUES (?, ?, ?, NULL, ?, NULL, 'GAME', NULL, NULL, ?, 0, ?, ?)
                """,
                (
                    clean_title,
                    cover_url,
                    cover_local,
                    date_completed,
                    created_at,
                    1 if has_platinum else 0,
                    1 if has_100 else 0,
                ),
            )
            inserted += 1

    conn.commit()
    conn.close()
    print(f"Inserted {inserted} games ({missing_cover} without cover art)")


# ---------------------------------------------------------------------------
# Zip building
# ---------------------------------------------------------------------------

def build_zip(source_zip: str, output_zip: str, db_path: str, new_cover_basenames: set[str]):
    with zipfile.ZipFile(source_zip, "r") as src, \
         zipfile.ZipFile(output_zip, "w", zipfile.ZIP_DEFLATED) as dst:

        # Copy everything except medialogger.db and game covers we're replacing
        for info in src.infolist():
            if info.filename == "medialogger.db":
                continue
            dst.writestr(info, src.read(info.filename))

        # Patched DB
        dst.write(db_path, "medialogger.db")

        # New game covers
        added = 0
        for basename in new_cover_basenames:
            local_path = os.path.join(LOCAL_COVERS_DIR, basename)
            if os.path.exists(local_path):
                dst.write(local_path, f"media_covers/{basename}")
                added += 1

    print(f"Added {added} game cover images to zip")
    print(f"Created: {output_zip}")


# ---------------------------------------------------------------------------
# Main
# ---------------------------------------------------------------------------

def main():
    if not RAWG_API_KEY:
        print("Error: set the RAWG_API_KEY environment variable.")
        print("Get a free key at https://rawg.io/apiv2")
        sys.exit(1)

    source_zip = sys.argv[1] if len(sys.argv) > 1 else os.path.expanduser(
        "~/Downloads/lifeos_backup_patched.zip"
    )
    output_zip = sys.argv[2] if len(sys.argv) > 2 else os.path.join(
        SCRIPT_DIR, "lifeos_backup_with_games.zip"
    )

    if not os.path.exists(source_zip):
        print(f"Error: source zip not found: {source_zip}")
        sys.exit(1)

    # Collect all unique clean titles for cover fetching
    unique_titles: list[str] = []
    seen: set[str] = set()
    for raw_titles in GAMES_BY_YEAR.values():
        for raw in raw_titles:
            clean, _, _ = parse_flags(raw)
            if clean not in seen:
                seen.add(clean)
                unique_titles.append(clean)

    total_entries = sum(len(v) for v in GAMES_BY_YEAR.values())
    print(f"Games to import: {total_entries} entries, {len(unique_titles)} unique titles")

    # Fetch cover URLs from RAWG (deduplicated)
    cover_url_map = build_cover_url_map(unique_titles, RAWG_API_KEY)

    # Download images (one per unique title, reused for duplicates)
    local_cover_map = download_covers(cover_url_map)

    # Extract DB from source zip
    db_path = os.path.join(SCRIPT_DIR, "medialogger.db")
    db_backup = db_path + ".bak"
    print(f"\nExtracting DB from {source_zip}...")
    with zipfile.ZipFile(source_zip, "r") as z:
        z.extract("medialogger.db", SCRIPT_DIR)
    shutil.copy2(db_path, db_backup)
    print(f"DB backed up to {db_backup}")

    # Patch DB
    patch_db(db_path, local_cover_map, cover_url_map)

    # Build output zip
    new_basenames = set(local_cover_map.values())
    build_zip(source_zip, output_zip, db_path, new_basenames)

    print("\nDone! To restore on device:")
    print(f"  1. adb push {output_zip} /sdcard/")
    print(f"     or copy to phone manually")
    print(f"  2. In app: Settings > Backup > Restore")


if __name__ == "__main__":
    main()
