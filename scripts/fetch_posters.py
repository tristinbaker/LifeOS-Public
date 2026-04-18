#!/usr/bin/env python3
"""Fetch poster URLs - batched and parallel"""

import json
from concurrent.futures import ThreadPoolExecutor, as_completed
from letterboxdpy.movie import Movie

INPUT_FILE = "movies_import.json"

def fetch_poster(args):
    """Fetch a single poster URL."""
    idx, slug = args
    if not slug:
        return idx, None
    try:
        m = Movie(f"https://letterboxd.com/film/{slug}/")
        poster = m.get_poster()
        return idx, poster
    except:
        return idx, None

def fetch_all():
    with open(INPUT_FILE, "r") as f:
        movies = json.load(f)

    slugs = [(i, m["slug"]) for i, m in enumerate(movies) if not m.get("coverUrl") and m.get("slug")]

    print(f"Fetching {len(slugs)} poster URLs...")

    updated = 0
    with ThreadPoolExecutor(max_workers=20) as executor:
        futures = {executor.submit(fetch_poster, args): args[0] for args in slugs}

        for future in as_completed(futures):
            idx, poster = future.result()
            if poster:
                movies[idx]["coverUrl"] = poster
                updated += 1

            if updated % 100 == 0:
                print(f"  {updated} updated...")

    print(f"Updated {updated} movies")

    with open(INPUT_FILE, "w") as f:
        json.dump(movies, f, indent=2)

if __name__ == "__main__":
    fetch_all()