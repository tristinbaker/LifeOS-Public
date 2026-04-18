#!/usr/bin/env python3
"""
Import books (2022-2025) into medialogger.db.
Fetches cover art via Open Library (no key needed), falls back to Google Books.
2026 books/manga are NOT touched.

Usage:
    python import_books.py [source_backup.zip] [output_backup.zip]

Defaults:
    source: ~/Downloads/lifeos_backup_with_games.zip
    output: scripts/lifeos_backup_with_books.zip
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

# ---------------------------------------------------------------------------
# Books data  (format: "Title by Author")
# ---------------------------------------------------------------------------

BOOKS_BY_YEAR = {
    2022: [
        "The Hobbit by J.R.R. Tolkien",
        "Storm Front (The Dresden Files #1) by Jim Butcher",
        "Gender Queer by Maia Kobabe",
        "Fool Moon by Jim Butcher",
        "I'd Like to Play Alone, Please by Tom Segura",
        "The Forests of Silence by Emily Rodda",
        "Calico Joe by John Grisham",
        "The Hitchhiker's Guide to the Galaxy by Douglas Adams",
        "Professional Idiot by Steve-O",
        "Legends & Lattes by Travis Baldree",
        "Harry Potter and the Sorcerer's Stone by J.K. Rowling",
        "Harry Potter and the Chamber of Secrets by J. K. Rowling",
        "Harry Potter and the Prisoner of Azkaban by J. K. Rowling",
    ],
    2023: [
        "The Fellowship of the Ring by J. R. R. Tolkien",
        "Grave Peril by Jim Butcher",
        "The Restaurant at the End of the Universe by Douglas Adams",
        "Magician's Nephew by C.S. Lewis",
        "The Lion, The Witch, and The Wardrobe by C. S. Lewis",
        "Love Wins by Rob Bell",
        "A Wizard's Guide to Defensive Baking by T. Kingfisher",
        "The Wonderful Wizard of Oz by Frank Baum",
    ],
    2024: [
        "Egghead by Bo Burnham",
        "Story of a Soul by St. Therese",
        "Rome Sweet Home by Scott and Kimberly Hahn",
        "Carrie by Stephen King",
        "Salem's Lot by Stephen King",
        "Rage by Richard Bachman",
        "The Exorcist by William Peter Blatty",
        "Dead Inside by Chandler Morrison",
    ],
    2025: [
        "The Bad Beginning by Lemony Snickett",
        "Summer Knight by Jim Butcher",
        "Death Masks by Jim Butcher",
        "Blood Rites by Jim Butcher",
        "I'm Thinking of Ending Things by Iain Reid",
        "Dead Beat by Jim Butcher",
        "Misery by Stephen King",
        "American Psycho by Bret Easton Ellis",
        "The Shining by Stephen King",
        "Harry Potter and the Goblet of Fire by J. K. Rowling",
        "Harry Potter and the Order of the Phoenix by J. K. Rowling",
        "Harry Potter and the Half-Blood Prince by J. K. Rowling",
        "The Hellbound Heart by Clive Barker",
        "Ichi the Killer Vol. 1 by Hideo Yamamoto",
        "Uzumaki by Junji Ito",
        "Sensor by Junji Ito",
        "Harry Potter and the Deathly Hallows by J. K. Rowling",
        "The Reptile Room by Lemony Snicket",
        "A History of Violence by John Wagner",
        "The Wide Window by Lemony Snicket",
        "Tomie by Junji Ito",
        "The Princess Bride by William Goldman",
        "Proven Guilty by Jim Butcher",
        "White Night by Jim Butcher",
        "Small Favor by Jim Butcher",
        "Turn Coat by Jim Butcher",
        "Ichi the Killer Vol. 2 by Hideo Yamamoto",
        "Changes by Jim Butcher",
        "Aftermath by Jim Butcher",
        "Ghost Story by Jim Butcher",
        "Welcome to the Jungle by Jim Butcher",
        "Storm Front (Comic) by Jim Butcher",
        "Legends in Exile by Bill Willingham",
        "Animal Farm by Bill Willingham",
        "Storybook Love by Bill Willingham",
        "Brat Pack by Rick Veitch",
        "March of the Wooden Soldiers by Bill Willingham",
        "The Mean Seasons by Bill Willingham",
    ],
}

# ---------------------------------------------------------------------------
# Parsing
# ---------------------------------------------------------------------------

def parse_entry(raw: str) -> tuple[str, str]:
    """Return (title, author) from 'Title by Author'. Splits on last ' by '."""
    idx = raw.rfind(" by ")
    if idx == -1:
        return raw.strip(), ""
    return raw[:idx].strip(), raw[idx + 4:].strip()


def year_to_timestamp_ms(year: int) -> int:
    dt = datetime(year, 12, 31, 12, 0, 0, tzinfo=timezone.utc)
    return int(dt.timestamp() * 1000)


# ---------------------------------------------------------------------------
# Cover fetching
# ---------------------------------------------------------------------------

def fetch_cover_url(title: str, author: str, retries: int = 2) -> str | None:
    """Try Open Library first, fall back to Google Books."""
    url = _open_library(title, author, retries)
    if url:
        return url
    return _google_books(title, author, retries)


def _open_library(title: str, author: str, retries: int) -> str | None:
    for attempt in range(retries):
        try:
            r = requests.get(
                "https://openlibrary.org/search.json",
                params={"title": title, "author": author, "limit": 5, "fields": "cover_i,title"},
                headers={"User-Agent": "LifeOS/1.0"},
                timeout=15,
            )
            if r.status_code == 429:
                time.sleep(2 ** attempt)
                continue
            for doc in r.json().get("docs", []):
                if doc.get("cover_i"):
                    return f"https://covers.openlibrary.org/b/id/{doc['cover_i']}-L.jpg"
        except Exception as e:
            print(f"    OL error for '{title}': {e}")
            time.sleep(1)
    return None


def _google_books(title: str, author: str, retries: int) -> str | None:
    query = f'intitle:"{title}"'
    if author:
        query += f' inauthor:"{author}"'
    for attempt in range(retries):
        try:
            r = requests.get(
                "https://www.googleapis.com/books/v1/volumes",
                params={"q": query, "maxResults": 3, "printType": "books"},
                headers={"User-Agent": "LifeOS/1.0"},
                timeout=15,
            )
            if r.status_code == 429:
                time.sleep(2 ** attempt)
                continue
            for item in r.json().get("items", []):
                thumb = item.get("volumeInfo", {}).get("imageLinks", {}).get("thumbnail")
                if thumb:
                    # Upgrade to larger size and force https
                    thumb = re.sub(r'zoom=\d', 'zoom=3', thumb)
                    thumb = thumb.replace("http://", "https://")
                    return thumb
        except Exception as e:
            print(f"    GB error for '{title}': {e}")
            time.sleep(1)
    return None


def build_cover_url_map(unique_pairs: list[tuple[str, str]], max_workers: int = 6) -> dict[tuple, str]:
    """Return {(title, author): cover_url}."""
    print(f"Fetching cover URLs for {len(unique_pairs)} unique titles...")
    results: dict[tuple, str] = {}
    completed = 0

    with ThreadPoolExecutor(max_workers=max_workers) as ex:
        future_to_pair = {ex.submit(fetch_cover_url, t, a): (t, a) for t, a in unique_pairs}
        for future in as_completed(future_to_pair):
            pair = future_to_pair[future]
            url = future.result()
            if url:
                results[pair] = url
            completed += 1
            if completed % 10 == 0:
                print(f"  {completed}/{len(unique_pairs)} fetched...")

    print(f"  Cover URLs found: {len(results)}/{len(unique_pairs)}")
    return results


# ---------------------------------------------------------------------------
# Image downloading
# ---------------------------------------------------------------------------

def download_and_resize(url: str, dest: str) -> str | None:
    try:
        r = requests.get(url, headers={"User-Agent": "Mozilla/5.0"}, timeout=30)
        if r.status_code != 200:
            return None
        img = Image.open(BytesIO(r.content))
        if img.mode in ("RGBA", "P"):
            img = img.convert("RGB")
        if img.width > MAX_WIDTH:
            img = img.resize((MAX_WIDTH, int(MAX_WIDTH / img.width * img.height)), Image.LANCZOS)
        img.save(dest, "JPEG", quality=JPEG_QUALITY, optimize=True)
        return dest
    except Exception as e:
        print(f"    Download error: {e}")
        return None


def download_covers(cover_url_map: dict[tuple, str], max_workers: int = 8) -> dict[tuple, str]:
    """Return {(title, author): local_basename}."""
    os.makedirs(LOCAL_COVERS_DIR, exist_ok=True)
    print(f"Downloading {len(cover_url_map)} cover images...")
    local_map: dict[tuple, str] = {}
    completed = 0

    with ThreadPoolExecutor(max_workers=max_workers) as ex:
        futures = {}
        for pair, url in cover_url_map.items():
            dest = os.path.join(LOCAL_COVERS_DIR, f"{uuid.uuid4()}.jpg")
            futures[ex.submit(download_and_resize, url, dest)] = (pair, dest)

        for future in as_completed(futures):
            pair, dest = futures[future]
            result = future.result()
            if result:
                local_map[pair] = os.path.basename(result)
            completed += 1
            if completed % 10 == 0:
                print(f"  {completed}/{len(cover_url_map)} downloaded...")

    print(f"  Images saved: {len(local_map)}/{len(cover_url_map)}")
    return local_map


# ---------------------------------------------------------------------------
# Database
# ---------------------------------------------------------------------------

def patch_db(db_path: str, cover_url_map: dict[tuple, str], local_cover_map: dict[tuple, str]):
    conn = sqlite3.connect(db_path)
    cur = conn.cursor()

    # Only delete 2022-2025 books — 2026 stays untouched
    cur.execute("""
        DELETE FROM media_items
        WHERE type = 'BOOK'
          AND strftime('%Y', dateCompleted / 1000, 'unixepoch') IN ('2022','2023','2024','2025')
    """)
    print(f"Deleted {cur.rowcount} existing BOOK entries (2022-2025)")

    created_at = int(datetime.now(tz=timezone.utc).timestamp() * 1000)
    inserted = 0
    missing = 0

    for year, raw_entries in BOOKS_BY_YEAR.items():
        date_completed = year_to_timestamp_ms(year)
        for raw in raw_entries:
            title, author = parse_entry(raw)
            pair = (title, author)
            cover_url = cover_url_map.get(pair)
            local_basename = local_cover_map.get(pair)
            cover_local = f"{ANDROID_COVERS_PREFIX}/{local_basename}" if local_basename else None
            if not cover_url:
                missing += 1

            cur.execute("""
                INSERT INTO media_items (
                    title, coverUrl, coverLocalPath, rating, dateCompleted,
                    notes, type, platform, author, createdAt,
                    isRewatch, hasPlatinum, has100Percent
                ) VALUES (?, ?, ?, NULL, ?, NULL, 'BOOK', NULL, ?, ?, 0, 0, 0)
            """, (title, cover_url, cover_local, date_completed, author, created_at))
            inserted += 1

    conn.commit()
    conn.close()
    print(f"Inserted {inserted} books ({missing} without cover art)")


# ---------------------------------------------------------------------------
# Zip building
# ---------------------------------------------------------------------------

def build_zip(source_zip: str, output_zip: str, db_path: str, new_basenames: set[str]):
    with zipfile.ZipFile(source_zip, "r") as src, \
         zipfile.ZipFile(output_zip, "w", zipfile.ZIP_DEFLATED) as dst:

        for info in src.infolist():
            if info.filename == "medialogger.db":
                continue
            dst.writestr(info, src.read(info.filename))

        dst.write(db_path, "medialogger.db")

        added = 0
        for basename in new_basenames:
            local = os.path.join(LOCAL_COVERS_DIR, basename)
            if os.path.exists(local):
                dst.write(local, f"media_covers/{basename}")
                added += 1

    print(f"Added {added} book cover images to zip")
    print(f"Created: {output_zip}")


# ---------------------------------------------------------------------------
# Main
# ---------------------------------------------------------------------------

def main():
    source_zip = sys.argv[1] if len(sys.argv) > 1 else os.path.expanduser(
        "~/Downloads/lifeos_backup_with_games.zip"
    )
    output_zip = sys.argv[2] if len(sys.argv) > 2 else os.path.join(
        SCRIPT_DIR, "lifeos_backup_with_books.zip"
    )

    if not os.path.exists(source_zip):
        print(f"Error: source zip not found: {source_zip}")
        sys.exit(1)

    # Collect unique (title, author) pairs for cover fetching
    unique_pairs: list[tuple[str, str]] = []
    seen: set[tuple[str, str]] = set()
    for raw_entries in BOOKS_BY_YEAR.values():
        for raw in raw_entries:
            pair = parse_entry(raw)
            if pair not in seen:
                seen.add(pair)
                unique_pairs.append(pair)

    total = sum(len(v) for v in BOOKS_BY_YEAR.values())
    print(f"Books to import: {total} entries, {len(unique_pairs)} unique titles")

    cover_url_map = build_cover_url_map(unique_pairs)
    local_cover_map = download_covers(cover_url_map)

    # Extract and back up DB
    db_path = os.path.join(SCRIPT_DIR, "medialogger.db")
    print(f"\nExtracting DB from {source_zip}...")
    with zipfile.ZipFile(source_zip, "r") as z:
        z.extract("medialogger.db", SCRIPT_DIR)
    shutil.copy2(db_path, db_path + ".bak")
    print(f"DB backed up to {db_path}.bak")

    patch_db(db_path, cover_url_map, local_cover_map)

    # Verify 2026 untouched
    conn = sqlite3.connect(db_path)
    rows = conn.execute(
        "SELECT strftime('%Y', dateCompleted/1000, 'unixepoch') AS yr, COUNT(*) "
        "FROM media_items WHERE type='BOOK' GROUP BY yr ORDER BY yr"
    ).fetchall()
    conn.close()
    print("Book counts by year:", dict(rows))

    new_basenames = set(local_cover_map.values())
    build_zip(source_zip, output_zip, db_path, new_basenames)

    # Copy to Downloads
    downloads_dest = os.path.expanduser(f"~/Downloads/{os.path.basename(output_zip)}")
    import shutil as _sh
    _sh.copy2(output_zip, downloads_dest)
    print(f"Copied to {downloads_dest}")


if __name__ == "__main__":
    main()
