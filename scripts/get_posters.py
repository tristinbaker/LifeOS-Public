#!/usr/bin/env python3
"""
Get posters for movies that don't have them.
Usage: python get_posters.py
"""

import json
import os
import uuid
import requests
from PIL import Image
from io import BytesIO
from concurrent.futures import ThreadPoolExecutor, as_completed
from letterboxdpy.movie import Movie

MEDIA_DIR = os.path.join(os.path.dirname(os.path.abspath(__file__)), "media_covers")
MAX_WIDTH = 400
JPEG_QUALITY = 70

def download_cover(url, filename):
    """Download cover, resize, compress, save."""
    try:
        headers = {"User-Agent": "Mozilla/5.0"}
        response = requests.get(url, headers=headers, timeout=30)
        if response.status_code != 200:
            return None

        img = Image.open(BytesIO(response.content))
        if img.mode in ("RGBA", "P"):
            img = img.convert("RGB")

        if img.width > MAX_WIDTH:
            aspect = img.width / img.height
            new_width = MAX_WIDTH
            new_height = int(new_width / aspect)
            img = img.resize((new_width, new_height), Image.LANCZOS)

        img.save(filename, "JPEG", quality=JPEG_QUALITY, optimize=True)
        return filename
    except Exception as e:
        print(f"Error: {e}")
        return None

def get_all_posters():
    os.makedirs(MEDIA_DIR, exist_ok=True)

    with open("movies_import.json", "r") as f:
        movies = json.load(f)

    movies_needing_posters = [m for m in movies if not m.get("coverUrl") or not m.get("coverLocalPath")]
    print(f"Getting posters for {len(movies_needing_posters)} movies...")

    cover_map = {}
    done = 0
    errors = 0

    for i, movie in enumerate(movies_needing_posters):
        slug = movie.get("slug")
        if not slug:
            continue

        try:
            m = Movie(f"https://letterboxd.com/film/{slug}/")
            poster_url = m.get_poster()
            if poster_url:
                filename = os.path.join(MEDIA_DIR, f"{uuid.uuid4()}.jpg")
                result = download_cover(poster_url, filename)
                if result:
                    cover_map[slug] = os.path.basename(result)
                    movie["coverUrl"] = poster_url
                    movie["coverLocalPath"] = os.path.basename(result)
        except Exception as e:
            errors += 1

        done += 1
        if done % 50 == 0:
            print(f"  Processed {done}/{len(movies_needing_posters)} (errors: {errors})")

    print(f"\nGot {len(cover_map)} posters ({errors} errors)")

    with open("movies_import.json", "w") as f:
        json.dump(movies, f, indent=2)

    return cover_map

if __name__ == "__main__":
    get_all_posters()