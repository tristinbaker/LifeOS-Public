#!/usr/bin/env python3
"""
Import movies from Letterboxd scrape into medialogger.db.
Usage: python import_movies.py <backup_zip_path>
"""

import json
import os
import sys
import uuid
import zipfile
import sqlite3
import requests
from PIL import Image
from io import BytesIO
from concurrent.futures import ThreadPoolExecutor, as_completed
from datetime import datetime

SCRIPT_DIR = os.path.dirname(os.path.abspath(__file__))
MEDIA_DIR = os.path.join(SCRIPT_DIR, "media_covers")
MAX_WIDTH = 400
JPEG_QUALITY = 70

def download_and_resize_cover(url, filename):
    """Download cover, resize to max 400px width, compress, save."""
    try:
        headers = {
            "User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36"
        }
        response = requests.get(url, headers=headers, timeout=30)
        if response.status_code != 200:
            return None

        img = Image.open(BytesIO(response.content))
        if img.mode in ("RGBA", "P"):
            img = img.convert("RGB")

        aspect = img.width / img.height
        if img.width > MAX_WIDTH:
            new_width = MAX_WIDTH
            new_height = int(new_width / aspect)
            img = img.resize((new_width, new_height), Image.LANCZOS)

        img.save(filename, "JPEG", quality=JPEG_QUALITY, optimize=True)
        return filename
    except Exception as e:
        print(f"    Error downloading {url}: {e}")
        return None

def process_covers(movies, max_workers=10):
    """Download all covers in parallel."""
    os.makedirs(MEDIA_DIR, exist_ok=True)

    cover_urls = [(m["title"], m["coverUrl"]) for m in movies if m.get("coverUrl")]
    print(f"Downloading {len(cover_urls)} covers...")

    results = {}
    completed = 0
    with ThreadPoolExecutor(max_workers=max_workers) as executor:
        futures = {}
        for title, url in cover_urls:
            filename = os.path.join(MEDIA_DIR, f"{uuid.uuid4()}.jpg")
            future = executor.submit(download_and_resize_cover, url, filename)
            futures[future] = (title, url, filename)

        for future in as_completed(futures):
            title, url, filename = futures[future]
            result = future.result()
            if result:
                basename = os.path.basename(result)
                results[url] = f"/data/user/0/com.tristinbaker.lifeos/files/media_covers/{basename}"
            completed += 1
            if completed % 50 == 0:
                print(f"  Downloaded {completed}/{len(cover_urls)} covers...")

    return results

def import_movies(backup_zip_path):
    """Import movies into database."""
    print(f"Reading scraped data...")
    with open("movies_import.json", "r", encoding="utf-8") as f:
        movies = json.load(f)

    print(f"Found {len(movies)} movies in scrape")

    cover_map = process_covers(movies)
    print(f"Covers downloaded: {len(cover_map)}")

    print(f"Extracting database from backup...")
    db_path = os.path.join(SCRIPT_DIR, "medialogger.db")
    zip_extract_dir = os.path.join(SCRIPT_DIR, "backup_extract")
    os.makedirs(zip_extract_dir, exist_ok=True)

    with zipfile.ZipFile(backup_zip_path, "r") as z:
        z.extract("medialogger.db", zip_extract_dir)
        extracted_db = os.path.join(zip_extract_dir, "medialogger.db")
        os.replace(extracted_db, db_path)

    conn = sqlite3.connect(db_path)
    cursor = conn.cursor()

    print("Deleting existing MOVIE entries...")
    cursor.execute("DELETE FROM media_items WHERE type = 'MOVIE'")
    deleted_count = cursor.rowcount
    print(f"  Deleted {deleted_count} movies")

    print("Inserting new movies...")
    created_at = int(datetime.now().timestamp() * 1000)
    insert_count = 0
    for movie in movies:
        title = movie.get("title")
        year = movie.get("year")
        rating = movie.get("rating")
        date_watched = movie.get("dateWatched")
        is_rewatch = movie.get("isRewatch", 0)
        review = movie.get("review")
        cover_url = movie.get("coverUrl")

        cover_local = cover_map.get(cover_url) if cover_url else None

        cursor.execute("""
            INSERT INTO media_items (
                title, coverUrl, coverLocalPath, rating, dateCompleted, notes, type,
                platform, author, createdAt, isRewatch, hasPlatinum, has100Percent
            ) VALUES (?, ?, ?, ?, ?, ?, 'MOVIE', NULL, NULL, ?, ?, 0, 0)
        """, (
            title,
            cover_url,
            cover_local,
            rating,
            date_watched,
            review,
            created_at,
            is_rewatch
        ))
        insert_count += 1

    conn.commit()
    print(f"  Inserted {insert_count} movies")

    cursor.execute("SELECT COUNT(*) FROM media_items WHERE type = 'MOVIE'")
    total = cursor.fetchone()[0]
    print(f"Total movies in DB: {total}")

    conn.close()

    print("\nDone! To restore:")
    print(f"  1. Create a new ZIP with medialogger.db and media_covers/")
    print(f"  2. In app: Settings > Backup > Restore")
    return db_path

if __name__ == "__main__":
    if len(sys.argv) < 2:
        print("Usage: python import_movies.py <backup_zip_path>")
        sys.exit(1)

    backup_path = sys.argv[1]
    if not os.path.exists(backup_path):
        print(f"Error: File not found: {backup_path}")
        sys.exit(1)

    import_movies(backup_path)