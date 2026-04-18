#!/usr/bin/env python3
"""
Scrape all films from Letterboxd user diary with reviews.
Usage: python scrape_letterboxd.py
Output: movies_import.json
"""

import json
import os
import time
from datetime import datetime
from zoneinfo import ZoneInfo
from letterboxdpy.user import User

USERNAME = "suqei"
OUTPUT_FILE = "movies_import.json"
TZ = ZoneInfo("America/New_York")

def scrape_diary():
    print(f"Fetching diary for user: {USERNAME}")
    user = User(USERNAME)
    diary = user.get_diary()
    entries = diary.get("entries", {})

    print(f"Fetching user reviews...")
    reviews_result = user.get_reviews()
    reviews_dict = reviews_result.get("reviews", {})

    print(f"Found {len(entries)} diary entries, {len(reviews_dict)} reviews")

    movies = []
    poster_urls = {}  # Cache of slug -> poster_url

    for i, (entry_id, entry) in enumerate(entries.items()):
        title = entry.get("name")
        year = entry.get("release")
        slug = entry.get("slug")

        actions = entry.get("actions", {})
        rating = actions.get("rating")
        is_rewatch = 1 if actions.get("rewatched", False) else 0

        date_str = entry.get("date")
        date_watched = None
        if date_str:
            try:
                dt = datetime.fromisoformat(date_str.replace("Z", "+00:00"))
                dt = dt.astimezone(TZ)
                date_watched = int(dt.timestamp() * 1000)
            except Exception as e:
                print(f"  Warning: Could not parse date {date_str}: {e}")

        review_text = None
        review_entry = reviews_dict.get(entry_id)
        if review_entry:
            review_data = review_entry.get("review", {})
            if review_data and review_data.get("content"):
                review_text = review_data.get("content", "")

        movie = {
            "title": title,
            "year": year,
            "rating": rating,
            "dateWatched": date_watched,
            "isRewatch": is_rewatch,
            "review": review_text,
            "coverUrl": None,
            "slug": slug
        }
        movies.append(movie)
        print(f"  [{i+1}/{len(entries)}] {title} ({year}) - rating: {rating}, rewatch: {is_rewatch}, has review: {bool(review_text)}")

    print(f"\nTotal films scraped: {len(movies)}")

    with open(OUTPUT_FILE, "w", encoding="utf-8") as f:
        json.dump(movies, f, indent=2, ensure_ascii=False)

    print(f"Saved to {OUTPUT_FILE}")
    print("\nNow run: python get_posters.py")
    return movies

if __name__ == "__main__":
    scrape_diary()