#!/usr/bin/env python3
"""Download all covers in parallel"""

import json
import os
import uuid
import requests
from PIL import Image
from io import BytesIO
from concurrent.futures import ThreadPoolExecutor, as_completed

MEDIA_DIR = "media_covers"
MAX_WIDTH = 400
JPEG_QUALITY = 70
INPUT_FILE = "movies_import.json"

def download_cover(args):
    """Download single cover."""
    idx, url = args
    if not url:
        return idx, None, None

    try:
        headers = {"User-Agent": "Mozilla/5.0"}
        response = requests.get(url, headers=headers, timeout=30)
        if response.status_code != 200:
            return idx, None, None

        img = Image.open(BytesIO(response.content))
        if img.mode in ("RGBA", "P"):
            img = img.convert("RGB")

        if img.width > MAX_WIDTH:
            aspect = img.width / img.height
            new_height = int(MAX_WIDTH / aspect)
            img = img.resize((MAX_WIDTH, new_height), Image.LANCZOS)

        filename = f"{uuid.uuid4()}.jpg"
        filepath = os.path.join(MEDIA_DIR, filename)
        img.save(filepath, "JPEG", quality=JPEG_QUALITY, optimize=True)
        return idx, filename, url
    except Exception as e:
        return idx, None, None

def main():
    os.makedirs(MEDIA_DIR, exist_ok=True)

    with open(INPUT_FILE, "r") as f:
        movies = json.load(f)

    urls = [(i, m.get("coverUrl")) for i, m in enumerate(movies) if m.get("coverUrl")]

    print(f"Downloading {len(urls)} covers...")

    count = 0
    with ThreadPoolExecutor(max_workers=15) as executor:
        futures = {executor.submit(download_cover, args): args[0] for args in urls}

        for future in as_completed(futures):
            idx, filename, url = future.result()
            if filename:
                movies[idx]["coverLocalPath"] = filename
                count += 1

            if count % 100 == 0:
                print(f"  {count} downloaded...")

    print(f"Downloaded {count} covers")

    with open(INPUT_FILE, "w") as f:
        json.dump(movies, f, indent=2)

if __name__ == "__main__":
    main()