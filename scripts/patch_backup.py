#!/usr/bin/env python3
"""
One-off script to fix the movie poster paths in medialogger.db and
produce a corrected backup zip.

What it fixes:
  - coverLocalPath for movies was stored as bare 'uuid.jpg' instead of
    the full Android path '/data/user/0/com.tristinbaker.lifeos/files/media_covers/uuid.jpg'
  - The movie cover files were not included in the backup zip

Usage:
  python patch_backup.py [source_backup.zip] [output_backup.zip]
  Defaults: lifeos_backup_final.zip → lifeos_backup_patched.zip
"""

import os
import sys
import shutil
import sqlite3
import zipfile

SCRIPT_DIR = os.path.dirname(os.path.abspath(__file__))
LOCAL_COVERS_DIR = os.path.join(SCRIPT_DIR, "media_covers")
ANDROID_COVERS_PREFIX = "/data/user/0/com.tristinbaker.lifeos/files/media_covers"

def patch_db(db_path):
    conn = sqlite3.connect(db_path)
    cursor = conn.cursor()

    cursor.execute(
        "SELECT id, coverLocalPath FROM media_items WHERE type='MOVIE' AND coverLocalPath IS NOT NULL AND coverLocalPath NOT LIKE '/%'"
    )
    rows = cursor.fetchall()
    print(f"Movies with bare-filename coverLocalPath: {len(rows)}")

    fixed = 0
    nulled = 0
    for row_id, bare_name in rows:
        local_path = os.path.join(LOCAL_COVERS_DIR, bare_name)
        if os.path.exists(local_path):
            cursor.execute(
                "UPDATE media_items SET coverLocalPath = ? WHERE id = ?",
                (f"{ANDROID_COVERS_PREFIX}/{bare_name}", row_id),
            )
            fixed += 1
        else:
            # File not available locally; NULL it so app falls back to coverUrl
            cursor.execute(
                "UPDATE media_items SET coverLocalPath = NULL WHERE id = ?",
                (row_id,),
            )
            nulled += 1

    conn.commit()
    print(f"  Fixed to full Android path: {fixed}")
    print(f"  Nulled (missing file, will use coverUrl): {nulled}")
    conn.close()

def build_backup(source_zip, output_zip, db_path):
    """
    Build a new backup zip:
      - All contents of source_zip, except medialogger.db
      - Patched medialogger.db
      - All media_covers referenced by movies (from local covers dir)
    """
    conn = sqlite3.connect(db_path)
    cursor = conn.cursor()
    cursor.execute("SELECT coverLocalPath FROM media_items WHERE coverLocalPath IS NOT NULL")
    all_paths = [row[0] for row in cursor.fetchall()]
    conn.close()

    referenced_basenames = {p.split("/")[-1] for p in all_paths}

    # Find which referenced covers exist locally (movies)
    local_movie_covers = {
        name: os.path.join(LOCAL_COVERS_DIR, name)
        for name in referenced_basenames
        if os.path.exists(os.path.join(LOCAL_COVERS_DIR, name))
    }
    print(f"Movie covers to include from local dir: {len(local_movie_covers)}")

    with zipfile.ZipFile(source_zip, "r") as src, zipfile.ZipFile(output_zip, "w", zipfile.ZIP_DEFLATED) as dst:
        # Copy everything except medialogger.db
        for item in src.infolist():
            if item.filename == "medialogger.db":
                continue
            dst.writestr(item, src.read(item.filename))

        # Add patched DB
        dst.write(db_path, "medialogger.db")
        print("Added patched medialogger.db")

        # Add movie covers (books/games covers already in source zip)
        for basename, local_path in local_movie_covers.items():
            dst.write(local_path, f"media_covers/{basename}")

    print(f"Created: {output_zip}")

def main():
    source_zip = sys.argv[1] if len(sys.argv) > 1 else os.path.join(SCRIPT_DIR, "lifeos_backup_final.zip")
    output_zip = sys.argv[2] if len(sys.argv) > 2 else os.path.join(SCRIPT_DIR, "lifeos_backup_patched.zip")

    if not os.path.exists(source_zip):
        print(f"Error: source zip not found: {source_zip}")
        sys.exit(1)

    # Work on a copy of the DB so we don't mutate the original
    db_path = os.path.join(SCRIPT_DIR, "medialogger.db")
    db_backup = db_path + ".bak"
    shutil.copy2(db_path, db_backup)
    print(f"DB backed up to {db_backup}")

    patch_db(db_path)
    build_backup(source_zip, output_zip, db_path)

    print("\nDone! To restore on device:")
    print("  1. Copy lifeos_backup_patched.zip to your phone")
    print("  2. In app: Settings > Backup > Restore")

if __name__ == "__main__":
    main()
