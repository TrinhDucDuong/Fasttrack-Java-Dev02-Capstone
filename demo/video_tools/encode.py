"""Encode each frame into an exact-length segment, then join without re-encoding.

usage: python encode.py <workdir> <out.mp4>
"""
import json
import subprocess
import sys
from concurrent.futures import ThreadPoolExecutor
from pathlib import Path

WORK, OUT = Path(sys.argv[1]), Path(sys.argv[2])
FPS = 30
timeline = json.loads((WORK / "timeline.json").read_text(encoding="utf-8"))
SEG = WORK / "segments"
SEG.mkdir(exist_ok=True)


def encode(item):
    i = item["frame"]
    frames = round((item["end"] - item["start"]) * FPS)
    target = SEG / f"{i:03d}.mp4"
    subprocess.run(["ffmpeg", "-y", "-loglevel", "error", "-loop", "1", "-framerate", str(FPS),
                    "-i", str(WORK / "png" / f"{i:03d}.png"), "-frames:v", str(frames),
                    "-c:v", "libx264", "-preset", "medium", "-crf", "20", "-tune", "stillimage",
                    "-pix_fmt", "yuv420p", "-r", str(FPS), str(target)], check=True)
    return frames


with ThreadPoolExecutor(max_workers=4) as pool:
    total_frames = sum(pool.map(encode, timeline))

(WORK / "segments.txt").write_text("".join(f"file 'segments/{t['frame']:03d}.mp4'\n" for t in timeline), encoding="utf-8")
seconds = total_frames / FPS
subprocess.run(["ffmpeg", "-y", "-loglevel", "error", "-f", "concat", "-safe", "0", "-i", str(WORK / "segments.txt"),
                "-f", "lavfi", "-t", f"{seconds:.3f}", "-i", "anullsrc=channel_layout=stereo:sample_rate=48000",
                "-c:v", "copy", "-c:a", "aac", "-b:a", "96k", "-movflags", "+faststart", str(OUT)], check=True)
print(f"{total_frames} frames = {seconds:.2f}s")
