"""Encode frames (timeline2.json) to exact-length segments, join, and mux the Vbee voice-over."""
import json, subprocess, sys
from concurrent.futures import ThreadPoolExecutor
from pathlib import Path

WORK, AUDIO, OUT = Path(sys.argv[1]), Path(sys.argv[2]), Path(sys.argv[3])
FPS = 30
timeline = json.loads((WORK / "timeline2.json").read_text(encoding="utf-8"))
audio_len = float(subprocess.run(["ffprobe", "-v", "error", "-show_entries", "format=duration", "-of", "csv=p=0", str(AUDIO)],
                                 capture_output=True, text=True).stdout)
timeline[-1]["end"] = max(timeline[-1]["end"], audio_len + 1.0)   # let the closing line finish
SEG = WORK / "segments2"; SEG.mkdir(exist_ok=True)

def encode(item):
    frames = round((item["end"] - item["start"]) * FPS)
    subprocess.run(["ffmpeg", "-y", "-loglevel", "error", "-loop", "1", "-framerate", str(FPS), "-i", str(WORK / "png2" / f"{item['frame']:03d}.png"),
                    "-frames:v", str(frames), "-c:v", "libx264", "-preset", "medium", "-crf", "20", "-tune", "stillimage",
                    "-pix_fmt", "yuv420p", "-threads", "2", "-r", str(FPS), str(SEG / f"{item['frame']:03d}.mp4")], check=True)
    return frames

with ThreadPoolExecutor(max_workers=1) as pool:
    total = sum(pool.map(encode, timeline))
(WORK / "segments2.txt").write_text("".join(f"file 'segments2/{t['frame']:03d}.mp4'\n" for t in timeline), encoding="utf-8")
seconds = total / FPS
subprocess.run(["ffmpeg", "-y", "-loglevel", "error", "-f", "concat", "-safe", "0", "-i", str(WORK / "segments2.txt"), "-i", str(AUDIO),
                "-filter_complex", f"[1:a]aresample=48000,apad=whole_dur={seconds:.3f}[a]", "-map", "0:v", "-map", "[a]",
                "-c:v", "copy", "-c:a", "aac", "-b:a", "160k", "-t", f"{seconds:.3f}", "-movflags", "+faststart", str(OUT)], check=True)
(WORK / "timeline2.json").write_text(json.dumps(timeline, ensure_ascii=False, indent=1), encoding="utf-8")
print(f"{len(timeline)} frames, {seconds:.1f}s")
