#!/usr/bin/env python3
"""Generate Android drawable assets from django/static/logo.png."""
from __future__ import annotations

import sys
from pathlib import Path

from PIL import Image

ROOT = Path(__file__).resolve().parents[3]
LOGO_SRC = ROOT / "django" / "static" / "logo.png"
DRAWABLE = Path(__file__).resolve().parents[1] / "app" / "src" / "main" / "res" / "drawable"


def _fit_logo(source: Image.Image, canvas_size: int, fill_ratio: float) -> Image.Image:
    canvas = Image.new("RGBA", (canvas_size, canvas_size), (0, 0, 0, 0))
    logo_w, logo_h = source.size
    scale = min(canvas_size * fill_ratio / logo_w, canvas_size * fill_ratio / logo_h)
    new_w = max(1, int(logo_w * scale))
    new_h = max(1, int(logo_h * scale))
    resized = source.resize((new_w, new_h), Image.Resampling.LANCZOS)
    x = (canvas_size - new_w) // 2
    y = (canvas_size - new_h) // 2
    canvas.paste(resized, (x, y), resized)
    return canvas


def _white_silhouette(source: Image.Image, canvas_size: int, fill_ratio: float, alpha_threshold: int = 24) -> Image.Image:
    canvas = Image.new("RGBA", (canvas_size, canvas_size), (0, 0, 0, 0))
    logo_w, logo_h = source.size
    scale = min(canvas_size * fill_ratio / logo_w, canvas_size * fill_ratio / logo_h)
    new_w = max(1, int(logo_w * scale))
    new_h = max(1, int(logo_h * scale))
    resized = source.resize((new_w, new_h), Image.Resampling.LANCZOS)
    white = Image.new("RGBA", (new_w, new_h), (0, 0, 0, 0))
    src_px = resized.load()
    dst_px = white.load()
    for py in range(new_h):
        for px in range(new_w):
            _, _, _, alpha = src_px[px, py]
            if alpha > alpha_threshold:
                dst_px[px, py] = (255, 255, 255, alpha)
    x = (canvas_size - new_w) // 2
    y = (canvas_size - new_h) // 2
    canvas.paste(white, (x, y), white)
    return canvas


def main() -> int:
    if not LOGO_SRC.is_file():
        print(f"Logo not found: {LOGO_SRC}", file=sys.stderr)
        return 1
    DRAWABLE.mkdir(parents=True, exist_ok=True)

    logo = Image.open(LOGO_SRC).convert("RGBA")
    brand = _fit_logo(logo, 512, 0.84)
    stat = _white_silhouette(logo, 96, 0.78)

    brand_path = DRAWABLE / "logo_divarfiling.png"
    stat_path = DRAWABLE / "ic_stat_divarfiling.png"
    brand.save(brand_path, optimize=True)
    stat.save(stat_path, optimize=True)

    print(f"Wrote {brand_path}")
    print(f"Wrote {stat_path}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
